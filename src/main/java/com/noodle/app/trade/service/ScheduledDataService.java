package com.noodle.app.trade.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.noodle.app.trade.entity.AccountEntity;
import com.noodle.app.trade.entity.AccountValueHistory;
import com.noodle.app.trade.entity.CryptoCurrencyEntity;
import com.noodle.app.trade.entity.HoldingEntity;
import com.noodle.app.trade.model.CryptoCurrency;
import com.noodle.app.trade.repository.AccountRepository;
import com.noodle.app.trade.repository.AccountValueHistoryRepository;
import com.noodle.app.trade.repository.CryptoCurrencyRepository;
import com.noodle.app.trade.repository.HoldingRepository;
import com.noodle.app.trade.util.TimeUtils;

@Service
public class ScheduledDataService {
    
    @Autowired
    private BinanceApiService binanceApiService;
    
    @Autowired
    private CryptoCurrencyRepository cryptoCurrencyRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private AccountValueHistoryRepository accountValueHistoryRepository;
    
    @Autowired
    private HoldingRepository holdingRepository;
    
    /**
     * 每30分钟执行一次的定时任务，获取最新的市场数据并保存到数据库
     */
    @Scheduled(fixedRate = 10 * 60 * 1000) // 30分钟 = 30 * 60 * 1000毫秒
    public void updateMarketData() {
        try {
            System.out.println("开始更新加密货币市场数据 - " + TimeUtils.getCurrentBeijingTimeFormatted());
            
            // 从Binance API获取所有市场数据
            List<CryptoCurrency> cryptoCurrencies = binanceApiService.getAllMarketData();
            
            // 保存到数据库
            for (CryptoCurrency crypto : cryptoCurrencies) {
                // 转换为实体对象
                CryptoCurrencyEntity entity = new CryptoCurrencyEntity();
                BeanUtils.copyProperties(crypto, entity);
                
                // 设置最后更新时间为当前北京时间
                entity.setLastUpdated(TimeUtils.getBeijingTime());
                
                // 保存到数据库
                cryptoCurrencyRepository.save(entity);
            }
            
            System.out.println("加密货币市场数据更新完成 - " + TimeUtils.getCurrentBeijingTimeFormatted());
            
            // 更新所有账户的总价值
            updateAllAccountValues();
            
        } catch (Exception e) {
            System.err.println("更新加密货币市场数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 更新所有账户的总价值并保存到历史记录中
     */
    private void updateAllAccountValues() {
        try {
            System.out.println("开始更新账户总价值 - " + TimeUtils.getCurrentBeijingTimeFormatted());
            
            // 获取所有账户
            List<AccountEntity> accounts = accountRepository.findAll();
            
            LocalDateTime currentTime = TimeUtils.getBeijingTime();
            
            for (AccountEntity account : accounts) {
                try {
                    // 计算账户总价值
                    BigDecimal totalValue = calculateAccountTotalValue(account);
                    
                    // 更新账户的总价值字段
                    account.setTotalValue(totalValue);
                    accountRepository.save(account);
                    
                    // 创建账户价值历史记录
                    AccountValueHistory valueHistory = new AccountValueHistory(
                        account.getAccountName(),
                        totalValue,
                        currentTime
                    );
                    
                    // 保存到数据库
                    accountValueHistoryRepository.save(valueHistory);
                    
                    System.out.println("账户 " + account.getAccountName() + " 总价值更新完成: " + totalValue);
                } catch (Exception e) {
                    System.err.println("更新账户 " + account.getAccountName() + " 总价值时发生错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("所有账户总价值更新完成 - " + TimeUtils.getCurrentBeijingTimeFormatted());
        } catch (Exception e) {
            System.err.println("更新账户总价值时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 计算账户总价值（余额 + 持仓价值）
     * @param account 账户实体
     * @return 账户总价值
     */
    private BigDecimal calculateAccountTotalValue(AccountEntity account) {
        BigDecimal totalValue = account.getBalance();
        
        try {
            // 获取账户持仓
            List<HoldingEntity> holdings = holdingRepository.findByAccountId(account.getId());
            
            // 计算每个持仓的价值
            for (HoldingEntity holding : holdings) {
                if (holding != null && holding.getQuantity() != null && holding.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    String symbol = holding.getSymbol();
                    BigDecimal quantity = holding.getQuantity();
                    
                    try {
                        // 获取当前价格
                        BigDecimal price = getCurrentPrice(symbol);
                        if (price != null) {
                            BigDecimal holdingValue = price.multiply(quantity);
                            totalValue = totalValue.add(holdingValue);
                        }
                    } catch (Exception e) {
                        System.err.println("获取币种 " + symbol + " 价格时发生错误: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("计算账户 " + account.getAccountName() + " 持仓价值时发生错误: " + e.getMessage());
        }
        
        return totalValue;
    }
    
    /**
     * 获取币种当前价格
     * @param symbol 币种符号
     * @return 当前价格
     */
    private BigDecimal getCurrentPrice(String symbol) {
        try {
            // 首先尝试从Binance API获取
            BigDecimal price = binanceApiService.getCurrentPrice(symbol + "USDT");
            return price;
        } catch (Exception e) {
            System.err.println("从Binance API获取 " + symbol + " 价格失败: " + e.getMessage());
            
            try {
                // 如果API获取失败，尝试从数据库获取最新价格
                List<CryptoCurrencyEntity> entities = cryptoCurrencyRepository.findBySymbolOrderByLastUpdatedDesc(symbol);
                if (!entities.isEmpty() && entities.get(0) != null) {
                    return entities.get(0).getPrice();
                }
            } catch (Exception dbException) {
                System.err.println("从数据库获取 " + symbol + " 价格失败: " + dbException.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * 每天凌晨2点执行的清理任务，删除超过30天的历史数据
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupOldData() {
        try {
            System.out.println("开始清理过期的市场数据 - " + TimeUtils.getCurrentBeijingTimeFormatted());
            
            // 计算30天前的北京时间
            LocalDateTime thirtyDaysAgo = TimeUtils.getBeijingTime().minusDays(30);
            
            // 删除超过30天的数据
            cryptoCurrencyRepository.deleteByLastUpdatedBefore(thirtyDaysAgo);
            
            // 删除超过30天的账户价值历史数据
            accountValueHistoryRepository.deleteByRecordTimeBefore(thirtyDaysAgo);
            
            System.out.println("过期市场数据清理完成 - " + TimeUtils.getCurrentBeijingTimeFormatted());
        } catch (Exception e) {
            System.err.println("清理过期市场数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}