package com.noodle.app.trade.api.controller;

import com.noodle.app.trade.entity.AccountEntity;
import com.noodle.app.trade.entity.AccountValueHistory;
import com.noodle.app.trade.entity.CryptoCurrencyEntity;
import com.noodle.app.trade.entity.HoldingEntity;
import com.noodle.app.trade.repository.AccountRepository;
import com.noodle.app.trade.repository.AccountValueHistoryRepository;
import com.noodle.app.trade.repository.CryptoCurrencyRepository;
import com.noodle.app.trade.repository.HoldingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

@RestController
@RequestMapping("/api/charts")
public class ChartController {
    
    @Autowired
    private CryptoCurrencyRepository cryptoCurrencyRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private HoldingRepository holdingRepository;
    
    @Autowired
    private AccountValueHistoryRepository accountValueHistoryRepository;
    
    /**
     * 获取指定币种的价格历史数据用于图表展示
     * @param symbol 币种符号
     * @param days 天数
     * @return 价格历史数据
     */
    @GetMapping("/price-history")
    public Map<String, Object> getPriceHistory(@RequestParam String symbol, @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 计算时间范围
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            // 处理币种符号，去掉USDT后缀用于数据库查询
            String dbSymbol = symbol;
            if (symbol.endsWith("USDT")) {
                dbSymbol = symbol.substring(0, symbol.length() - 5);
            }
            
            // 直接使用数据库查询方法获取时间范围内的数据，按时间升序排列
            List<CryptoCurrencyEntity> entities = cryptoCurrencyRepository.findBySymbolAndLastUpdatedBetweenOrderByLastUpdatedAsc(dbSymbol, startTime, endTime);
            
            // 准备图表数据
            List<Map<String, Object>> chartData = new ArrayList<>();
            for (CryptoCurrencyEntity entity : entities) {
                if (entity != null) {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("time", entity.getLastUpdated() != null ? entity.getLastUpdated().toString() : "");
                    dataPoint.put("price", entity.getPrice());
                    dataPoint.put("volume", entity.getVolume());
                    chartData.add(dataPoint);
                }
            }
            
            response.put("success", true);
            response.put("symbol", symbol);
            response.put("data", chartData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "获取价格历史数据失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取指定账户的持仓分布数据用于饼图展示
     * @param accountName 账户名称
     * @return 持仓分布数据
     */
    @GetMapping("/portfolio-distribution")
    public Map<String, Object> getPortfolioDistribution(@RequestParam String accountName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 查找账户
            Optional<AccountEntity> accountOptional = accountRepository.findByAccountName(accountName);
            if (!accountOptional.isPresent()) {
                response.put("success", false);
                response.put("error", "账户不存在: " + accountName);
                return response;
            }
            
            AccountEntity account = accountOptional.get();
            
            // 检查账户ID是否为空
            if (account.getId() == null) {
                response.put("success", false);
                response.put("error", "账户ID为空，请检查数据库");
                return response;
            }
            
            // 获取账户的持仓信息
            List<HoldingEntity> holdings = holdingRepository.findByAccountId(account.getId());
            
            // 准备图表数据
            List<Map<String, Object>> chartData = new ArrayList<>();
            
            // 如果没有持仓，返回空数据
            if (holdings.isEmpty()) {
                response.put("success", true);
                response.put("accountName", accountName);
                response.put("data", chartData);
                return response;
            }
            
            // 计算每个持仓的当前价值
            BigDecimal totalValue = BigDecimal.ZERO;
            List<Map<String, Object>> holdingValues = new ArrayList<>();
            
            for (HoldingEntity holding : holdings) {
                if (holding != null && holding.getSymbol() != null && holding.getQuantity() != null) {
                    String symbol = holding.getSymbol();
                    BigDecimal quantity = holding.getQuantity();
                    
                    // 获取当前价格 (注意数据库中存储的是不带USDT的符号)
                    List<CryptoCurrencyEntity> priceEntities = cryptoCurrencyRepository.findBySymbolOrderByLastUpdatedDesc(symbol);
                    if (priceEntities != null && !priceEntities.isEmpty() && priceEntities.get(0) != null) {
                        BigDecimal currentPrice = priceEntities.get(0).getPrice();
                        if (currentPrice != null) {
                            BigDecimal value = currentPrice.multiply(quantity);
                            totalValue = totalValue.add(value);
                            
                            Map<String, Object> holdingValue = new HashMap<>();
                            holdingValue.put("symbol", symbol);
                            holdingValue.put("quantity", quantity);
                            holdingValue.put("price", currentPrice);
                            holdingValue.put("value", value);
                            holdingValues.add(holdingValue);
                        }
                    }
                }
            }
            
            // 转换为百分比数据
            for (Map<String, Object> holdingValue : holdingValues) {
                String symbol = (String) holdingValue.get("symbol");
                Object valueObj = holdingValue.get("value");
                
                // 检查value是否为null
                if (valueObj == null) {
                    continue;
                }
                
                BigDecimal value = (BigDecimal) valueObj;
                
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("symbol", symbol);
                // 计算百分比，避免除以零
                if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal percentage = value.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, BigDecimal.ROUND_HALF_UP);
                    dataPoint.put("value", percentage);
                } else {
                    dataPoint.put("value", BigDecimal.ZERO);
                }
                chartData.add(dataPoint);
            }
            
            response.put("success", true);
            response.put("accountName", accountName);
            response.put("data", chartData);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "获取持仓分布数据失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取指定账户的价值历史数据用于图表展示
     * @param accountName 账户名称
     * @param days 天数
     * @return 账户价值历史数据
     */
    @GetMapping("/account-value-history")
    public Map<String, Object> getAccountValueHistory(@RequestParam String accountName, @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 计算时间范围
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            // 获取账户价值历史记录
            List<AccountValueHistory> valueHistories = accountValueHistoryRepository.findByAccountNameAndRecordTimeBetweenOrderByRecordTimeAsc(accountName, startTime, endTime);
            
            // 准备图表数据
            List<Map<String, Object>> chartData = new ArrayList<>();
            for (AccountValueHistory history : valueHistories) {
                if (history != null) {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("time", history.getRecordTime() != null ? history.getRecordTime().toString() : "");
                    dataPoint.put("value", history.getTotalValue());
                    chartData.add(dataPoint);
                }
            }
            
            response.put("success", true);
            response.put("accountName", accountName);
            response.put("data", chartData);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "获取账户价值历史数据失败: " + e.getMessage());
        }
        
        return response;
    }
}