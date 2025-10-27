package com.noodle.app.trade.api.controller;

import com.noodle.app.trade.entity.CryptoCurrencyEntity;
import com.noodle.app.trade.model.Account;
import com.noodle.app.trade.model.CryptoCurrency;
import com.noodle.app.trade.model.TradeRecord;
import com.noodle.app.trade.repository.CryptoCurrencyRepository;
import com.noodle.app.trade.service.BinanceApiService;
import com.noodle.app.trade.service.DeepSeekAiService;
import com.noodle.app.trade.service.TradeHistoryService;
import com.noodle.app.trade.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trading")
public class TradingController {
    
    @Autowired
    private BinanceApiService binanceApiService;
    
    @Autowired
    private TradingService tradingService;
    
    @Autowired
    private TradeHistoryService tradeHistoryService;
    
    @Autowired
    private DeepSeekAiService deepSeekAiService;
    
    @Autowired
    private CryptoCurrencyRepository cryptoCurrencyRepository;
    
    /**
     * 获取指定币种的当前价格
     * @param symbol 币种符号，如 "DOGEUSDT"
     * @return 当前价格
     */
    @GetMapping("/price/{symbol}")
    public Map<String, Object> getCurrentPrice(@PathVariable String symbol) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            BigDecimal price = binanceApiService.getCurrentPrice(symbol);
            response.put("success", true);
            response.put("symbol", symbol);
            response.put("price", price);
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "获取价格失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 创建模拟交易账户
     * @param accountName 账户名称
     * @param initialBalance 初始余额
     * @return 账户信息
     */
    @PostMapping("/account/create")
    public Map<String, Object> createAccount(@RequestParam String accountName, 
                                          @RequestParam BigDecimal initialBalance) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Account account = tradingService.createAccount(accountName, initialBalance);
            response.put("success", true);
            response.put("data", account);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "创建账户失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取账户信息
     * @param accountName 账户名称
     * @return 账户信息
     */
    @GetMapping("/account/{accountName}")
    public Map<String, Object> getAccount(@PathVariable String accountName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Account account = tradingService.getAccount(accountName);
            
            // 计算账户总价值
            BigDecimal totalValue = account.getBalance();
            for (Map.Entry<String, BigDecimal> entry : account.getHoldings().entrySet()) {
                String symbol = entry.getKey();
                BigDecimal quantity = entry.getValue();
                
                if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                    try {
                        BigDecimal price = binanceApiService.getCurrentPrice(symbol + "USDT");
                        BigDecimal holdingValue = price.multiply(quantity);
                        totalValue = totalValue.add(holdingValue);
                    } catch (Exception e) {
                        // 如果无法获取价格，使用默认价格
                        BigDecimal defaultPrice = BigDecimal.valueOf(100); // 默认价格
                        BigDecimal holdingValue = defaultPrice.multiply(quantity);
                        totalValue = totalValue.add(holdingValue);
                    }
                }
            }
            
            account.setTotalValue(totalValue);
            response.put("success", true);
            response.put("data", account);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "获取账户信息失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 执行买入交易
     * @param accountName 账户名称
     * @param symbol 币种符号
     * @param quantity 购买数量
     * @param strategy 使用的交易策略
     * @return 交易记录
     */
    @PostMapping("/trade/buy")
    public Map<String, Object> executeBuy(@RequestParam String accountName,
                                       @RequestParam String symbol,
                                       @RequestParam String quantity,
                                       @RequestParam String strategy) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证输入参数
            if (accountName == null || accountName.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "账户名称不能为空");
                return response;
            }
            
            if (symbol == null || symbol.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "币种符号不能为空");
                return response;
            }
            
            if (quantity == null || quantity.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "交易数量不能为空");
                return response;
            }
            
            // 转换数量为BigDecimal
            BigDecimal quantityValue;
            try {
                quantityValue = new BigDecimal(quantity);
                if (quantityValue.compareTo(BigDecimal.ZERO) <= 0) {
                    response.put("success", false);
                    response.put("error", "交易数量必须大于0");
                    return response;
                }
            } catch (NumberFormatException e) {
                response.put("success", false);
                response.put("error", "交易数量格式错误");
                return response;
            }
            
            // 获取当前币种价格
            List<String> symbols = java.util.Arrays.asList(symbol + "USDT");
            List<CryptoCurrency> cryptoList = null;
            
            try {
                cryptoList = binanceApiService.getMarketData(symbols);
            } catch (Exception e) {
                System.err.println("从Binance API获取数据失败: " + e.getMessage());
                // 如果从API获取失败，使用数据库中的数据
                cryptoList = new ArrayList<>();
            }
            
            // 如果API获取失败或没有数据，尝试从数据库获取
            if (cryptoList == null || cryptoList.isEmpty()) {
                System.out.println("尝试从数据库获取币种数据");
                // 从数据库获取最新数据
                String dbSymbol = symbol;
                if (symbol.endsWith("USDT")) {
                    dbSymbol = symbol.substring(0, symbol.length() - 5);
                }
                
                List<CryptoCurrencyEntity> entities = cryptoCurrencyRepository.findBySymbolOrderByLastUpdatedDesc(dbSymbol);
                if (!entities.isEmpty()) {
                    CryptoCurrencyEntity entity = entities.get(0);
                    CryptoCurrency crypto = new CryptoCurrency();
                    crypto.setSymbol(entity.getSymbol());
                    crypto.setName(entity.getName());
                    crypto.setPrice(entity.getPrice());
                    crypto.setPriceChange(entity.getPriceChange());
                    crypto.setPriceChangePercent(entity.getPriceChangePercent());
                    crypto.setVolume(entity.getVolume());
                    crypto.setLastUpdated(entity.getLastUpdated());
                    cryptoList.add(crypto);
                    System.out.println("从数据库获取到币种数据: " + crypto.getSymbol() + ", 价格: " + crypto.getPrice());
                }
            }
            
            if (cryptoList == null || cryptoList.isEmpty()) {
                response.put("success", false);
                response.put("error", "无法获取币种价格信息");
                return response;
            }
            
            CryptoCurrency crypto = cryptoList.get(0);
            if (crypto == null) {
                response.put("success", false);
                response.put("error", "币种信息获取失败");
                return response;
            }
            
            System.out.println("获取到币种信息: " + crypto.getSymbol() + ", 价格: " + crypto.getPrice());
            
            TradeRecord tradeRecord = tradingService.executeBuy(accountName, crypto, quantityValue, strategy);
            
            response.put("success", true);
            response.put("data", tradeRecord);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "买入交易失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 执行卖出交易
     * @param accountName 账户名称
     * @param symbol 币种符号
     * @param quantity 卖出数量
     * @param strategy 使用的交易策略
     * @return 交易记录
     */
    @PostMapping("/trade/sell")
    public Map<String, Object> executeSell(@RequestParam String accountName,
                                         @RequestParam String symbol,
                                         @RequestParam String quantity,
                                         @RequestParam String strategy) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证输入参数
            if (accountName == null || accountName.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "账户名称不能为空");
                return response;
            }
            
            if (symbol == null || symbol.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "币种符号不能为空");
                return response;
            }
            
            if (quantity == null || quantity.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "交易数量不能为空");
                return response;
            }
            
            // 转换数量为BigDecimal
            BigDecimal quantityValue;
            try {
                quantityValue = new BigDecimal(quantity);
                if (quantityValue.compareTo(BigDecimal.ZERO) <= 0) {
                    response.put("success", false);
                    response.put("error", "交易数量必须大于0");
                    return response;
                }
            } catch (NumberFormatException e) {
                response.put("success", false);
                response.put("error", "交易数量格式错误");
                return response;
            }
            
            // 获取当前币种价格
            List<String> symbols = java.util.Arrays.asList(symbol + "USDT");
            List<CryptoCurrency> cryptoList = null;
            
            try {
                cryptoList = binanceApiService.getMarketData(symbols);
            } catch (Exception e) {
                System.err.println("从Binance API获取数据失败: " + e.getMessage());
                // 如果从API获取失败，使用数据库中的数据
                cryptoList = new ArrayList<>();
            }
            
            // 如果API获取失败或没有数据，尝试从数据库获取
            if (cryptoList == null || cryptoList.isEmpty()) {
                System.out.println("尝试从数据库获取币种数据");
                // 从数据库获取最新数据
                String dbSymbol = symbol;
                if (symbol.endsWith("USDT")) {
                    dbSymbol = symbol.substring(0, symbol.length() - 5);
                }
                
                List<CryptoCurrencyEntity> entities = cryptoCurrencyRepository.findBySymbolOrderByLastUpdatedDesc(dbSymbol);
                if (!entities.isEmpty()) {
                    CryptoCurrencyEntity entity = entities.get(0);
                    CryptoCurrency crypto = new CryptoCurrency();
                    crypto.setSymbol(entity.getSymbol());
                    crypto.setName(entity.getName());
                    crypto.setPrice(entity.getPrice());
                    crypto.setPriceChange(entity.getPriceChange());
                    crypto.setPriceChangePercent(entity.getPriceChangePercent());
                    crypto.setVolume(entity.getVolume());
                    crypto.setLastUpdated(entity.getLastUpdated());
                    cryptoList.add(crypto);
                    System.out.println("从数据库获取到币种数据: " + crypto.getSymbol() + ", 价格: " + crypto.getPrice());
                }
            }
            
            if (cryptoList == null || cryptoList.isEmpty()) {
                response.put("success", false);
                response.put("error", "无法获取币种价格信息");
                return response;
            }
            
            CryptoCurrency crypto = cryptoList.get(0);
            if (crypto == null) {
                response.put("success", false);
                response.put("error", "币种信息获取失败");
                return response;
            }
            
            System.out.println("获取到币种信息: " + crypto.getSymbol() + ", 价格: " + crypto.getPrice());
            
            TradeRecord tradeRecord = tradingService.executeSell(accountName, crypto, quantityValue, strategy);
            
            response.put("success", true);
            response.put("data", tradeRecord);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "卖出交易失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取交易历史记录
     * @param accountName 账户名称
     * @return 交易历史记录
     */
    @GetMapping("/trade/history/{accountName}")
    public Map<String, Object> getTradeHistory(@PathVariable String accountName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Fetching trade history for account: " + accountName);
            // 根据账户名称获取交易记录
            List<TradeRecord> tradeRecords = tradeHistoryService.getTradeRecordsByAccountName(accountName);
            System.out.println("Trade records count: " + tradeRecords.size());
            response.put("success", true);
            response.put("data", tradeRecords);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "获取交易历史失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取所有交易历史记录
     * @return 所有交易历史记录
     */
    @GetMapping("/trade/history/all")
    public Map<String, Object> getAllTradeHistory() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TradeRecord> tradeRecords = tradeHistoryService.getAllTradeRecords();
            response.put("success", true);
            response.put("data", tradeRecords);
            response.put("count", tradeRecords.size());
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "获取所有交易记录失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取所有加密货币的实时市场数据
     * @return 加密货币市场数据列表
     */
    @GetMapping("/market-data")
    public Map<String, Object> getMarketData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<CryptoCurrency> marketData = binanceApiService.getAllMarketData();
            response.put("success", true);
            response.put("data", marketData);
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "获取市场数据失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 创建模拟交易账户
     * @param accountName 账户名称
     * @param initialBalance 初始余额
     * @return 账户信息
     */
    @PostMapping("/account")
    public Map<String, Object> createSimulatedAccount(@RequestParam String accountName, 
                                                   @RequestParam BigDecimal initialBalance) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Account account = tradingService.createAccount(accountName, initialBalance);
            response.put("success", true);
            response.put("data", account);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "创建账户失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取AI交易策略建议
     * @return AI策略建议
     */
    @GetMapping("/ai/strategy")
    public Map<String, Object> getAiStrategy() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<CryptoCurrency> marketData = binanceApiService.getAllMarketData();
            String strategy = deepSeekAiService.analyzeMarketData(marketData);
            
            response.put("success", true);
            response.put("data", strategy);
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "获取AI策略建议失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 重置模拟账户
     * @param accountName 账户名称
     * @param initialBalance 初始余额
     * @return 重置后的账户信息
     */
    @PostMapping("/account/reset")
    public Map<String, Object> resetAccount(@RequestParam String accountName, 
                                          @RequestParam BigDecimal initialBalance) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Account account = tradingService.resetAccount(accountName, initialBalance);
            response.put("success", true);
            response.put("data", account);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "重置账户失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 删除账户
     * @param accountName 账户名称
     * @return 删除结果
     */
    @DeleteMapping("/account/{accountName}")
    public Map<String, Object> deleteAccount(@PathVariable String accountName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = tradingService.deleteAccount(accountName);
            if (deleted) {
                response.put("success", true);
                response.put("message", "账户删除成功");
            } else {
                response.put("success", false);
                response.put("error", "账户不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "删除账户失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取指定币种的AI交易信号
     * @param symbol 币种符号
     * @return AI交易信号
     */
    @GetMapping("/ai/signal/{symbol}")
    public Map<String, Object> getAiSignal(@PathVariable String symbol) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> symbols = java.util.Arrays.asList(symbol + "USDT");
            List<CryptoCurrency> cryptoList = binanceApiService.getMarketData(symbols);
            
            if (cryptoList.isEmpty()) {
                response.put("success", false);
                response.put("error", "无法获取币种信息");
                return response;
            }
            
            CryptoCurrency crypto = cryptoList.get(0);
            String signal = deepSeekAiService.generateTradingSignal(crypto);
            
            response.put("success", true);
            response.put("data", signal);
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "获取AI交易信号失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取详细的AI交易建议
     * @return 包含每个币种交易建议的详细分析
     */
    @GetMapping("/ai/detailed-advice")
    public Map<String, Object> getDetailedAiAdvice() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<CryptoCurrency> marketData = binanceApiService.getAllMarketData();
            String advice = deepSeekAiService.generateDetailedTradingAdvice(marketData);
            
            response.put("success", true);
            response.put("data", advice);
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "获取AI详细交易建议失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取具体的AI交易建议，包括买入或卖出的数量
     * @param accountName 账户名称
     * @return 包含具体交易数量的建议
     */
    @GetMapping("/ai/specific-advice/{accountName}")
    public Map<String, Object> getSpecificAiAdvice(@PathVariable String accountName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取市场数据
            List<CryptoCurrency> marketData = binanceApiService.getAllMarketData();
            
            // 获取账户信息
            Account account = tradingService.getAccount(accountName);
            
            // 准备账户持仓信息
            Map<String, Double> accountHoldings = new HashMap<>();
            for (Map.Entry<String, BigDecimal> entry : account.getHoldings().entrySet()) {
                accountHoldings.put(entry.getKey(), entry.getValue().doubleValue());
            }
            
            // 获取账户可用余额
            double availableBalance = account.getBalance().doubleValue();
            
            // 生成具体的交易建议
            String advice = deepSeekAiService.generateSpecificTradingAdvice(marketData, accountHoldings, availableBalance);
            
            response.put("success", true);
            response.put("data", advice);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "获取AI具体交易建议失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取DeepSeek AI余额信息
     * @return API余额信息
     */
    @GetMapping("/ai/balance")
    public Map<String, Object> getAiBalance() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String balanceInfo = deepSeekAiService.getDeepSeekAiBalance();
            response.put("success", true);
            response.put("data", balanceInfo);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "获取AI余额信息失败: " + e.getMessage());
        }
        
        return response;
    }
}