package com.noodle.app.trade.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.noodle.app.trade.entity.TradeRecordEntity;
import com.noodle.app.trade.model.TradeRecord;
import com.noodle.app.trade.repository.TradeRecordRepository;

@Service
public class TradeHistoryService {
    
    @Autowired
    private TradeRecordRepository tradeRecordRepository;
    
    /**
     * 获取指定币种的交易记录
     * @param symbol 币种符号
     * @return 交易记录列表
     */
    public List<TradeRecord> getTradeRecordsBySymbol(String symbol) {
        List<TradeRecordEntity> entities = tradeRecordRepository.findBySymbol(symbol);
        return convertToModelList(entities);
    }
    
    /**
     * 获取指定交易类型的交易记录
     * @param tradeType 交易类型 (BUY/SELL)
     * @return 交易记录列表
     */
    public List<TradeRecord> getTradeRecordsByType(String tradeType) {
        List<TradeRecordEntity> entities = tradeRecordRepository.findByTradeType(tradeType);
        return convertToModelList(entities);
    }
    
    /**
     * 获取指定时间范围内的交易记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    public List<TradeRecord> getTradeRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<TradeRecordEntity> entities = tradeRecordRepository.findByTradeTimeBetween(startTime, endTime);
        return convertToModelList(entities);
    }
    
    /**
     * 获取指定币种和时间范围内的交易记录
     * @param symbol 币种符号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    public List<TradeRecord> getTradeRecordsBySymbolAndTimeRange(String symbol, LocalDateTime startTime, LocalDateTime endTime) {
        List<TradeRecordEntity> entities = tradeRecordRepository.findBySymbolAndTradeTimeBetween(symbol, startTime, endTime);
        return convertToModelList(entities);
    }
    
    /**
     * 获取最新的交易记录
     * @param limit 限制数量
     * @return 交易记录列表
     */
    public List<TradeRecord> getLatestTradeRecords(int limit) {
        List<TradeRecordEntity> entities = tradeRecordRepository.findLatestTrades(limit);
        return convertToModelList(entities);
    }
    
    /**
     * 获取所有交易记录
     * @return 交易记录列表
     */
    public List<TradeRecord> getAllTradeRecords() {
        List<TradeRecordEntity> entities = tradeRecordRepository.findAll();
        return convertToModelList(entities);
    }
    

    
    /**
     * 计算指定币种的总交易量
     * @param symbol 币种符号
     * @return 总交易量
     */
    public BigDecimal getTotalVolumeBySymbol(String symbol) {
        List<TradeRecordEntity> entities = tradeRecordRepository.findBySymbol(symbol);
        BigDecimal totalVolume = BigDecimal.ZERO;
        
        for (TradeRecordEntity entity : entities) {
            totalVolume = totalVolume.add(entity.getAmount());
        }
        
        return totalVolume;
    }
    
    /**
     * 计算指定币种的盈亏情况
     * @param symbol 币种符号
     * @return 盈亏金额
     */
    public BigDecimal calculateProfitLoss(String symbol) {
        List<TradeRecordEntity> allRecords = tradeRecordRepository.findBySymbol(symbol);
        List<TradeRecordEntity> buyRecords = new ArrayList<>();
        List<TradeRecordEntity> sellRecords = new ArrayList<>();
        
        // 分离买入和卖出记录
        for (TradeRecordEntity record : allRecords) {
            if ("BUY".equals(record.getTradeType())) {
                buyRecords.add(record);
            } else if ("SELL".equals(record.getTradeType())) {
                sellRecords.add(record);
            }
        }
        
        BigDecimal totalBuyAmount = BigDecimal.ZERO;
        BigDecimal totalSellAmount = BigDecimal.ZERO;
        BigDecimal totalBuyQuantity = BigDecimal.ZERO;
        BigDecimal totalSellQuantity = BigDecimal.ZERO;
        
        // 计算总买入金额和数量
        for (TradeRecordEntity record : buyRecords) {
            totalBuyAmount = totalBuyAmount.add(record.getAmount());
            totalBuyQuantity = totalBuyQuantity.add(record.getQuantity());
        }
        
        // 计算总卖出金额和数量
        for (TradeRecordEntity record : sellRecords) {
            totalSellAmount = totalSellAmount.add(record.getAmount());
            totalSellQuantity = totalSellQuantity.add(record.getQuantity());
        }
        
        // 计算盈亏（仅计算已完全卖出的部分）
        BigDecimal soldQuantity = totalBuyQuantity.min(totalSellQuantity);
        if (soldQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // 计算平均买入价格和卖出价格
        BigDecimal avgBuyPrice = totalBuyQuantity.compareTo(BigDecimal.ZERO) > 0 ? 
            totalBuyAmount.divide(totalBuyQuantity, 10, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        BigDecimal avgSellPrice = totalSellQuantity.compareTo(BigDecimal.ZERO) > 0 ? 
            totalSellAmount.divide(totalSellQuantity, 10, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        
        // 计算盈亏
        BigDecimal profitLoss = (avgSellPrice.subtract(avgBuyPrice)).multiply(soldQuantity);
        return profitLoss;
    }
    
    /**
     * 将实体列表转换为模型列表
     * @param entities 实体列表
     * @return 模型列表
     */
    private List<TradeRecord> convertToModelList(List<TradeRecordEntity> entities) {
        List<TradeRecord> records = new ArrayList<>();
        
        if (entities != null) {
            for (TradeRecordEntity entity : entities) {
                if (entity != null) {
                    System.out.println("Converting entity: " + entity.getSymbol() + ", " + entity.getTradeType() + ", " + entity.getAccountName());
                    System.out.println("Entity tradeTime: " + entity.getTradeTime());
                    try {
                        TradeRecord record = new TradeRecord(
                            entity.getSymbol() != null ? entity.getSymbol() : "",
                            entity.getTradeType() != null ? entity.getTradeType() : "",
                            entity.getPrice() != null ? entity.getPrice() : BigDecimal.ZERO,
                            entity.getQuantity() != null ? entity.getQuantity() : BigDecimal.ZERO,
                            entity.getAmount() != null ? entity.getAmount() : BigDecimal.ZERO,
                            entity.getTradeTime() != null ? entity.getTradeTime() : java.time.LocalDateTime.now(),
                            entity.getStrategy() != null ? entity.getStrategy() : ""
                        );
                        record.setId(entity.getId());
                        records.add(record);
                        System.out.println("Successfully converted entity");
                    } catch (Exception e) {
                        System.err.println("Error converting entity: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return records;
    }
    
    /**
     * 根据策略名称获取交易记录
     * @param strategy 策略名称（这里用作账户名称）
     * @return 交易记录列表
     */
    public List<TradeRecord> getTradeRecordsByStrategy(String strategy) {
        List<TradeRecordEntity> entities = tradeRecordRepository.findByStrategy(strategy);
        return convertToModelList(entities);
    }
    
    /**
     * 根据账户名称获取交易记录
     * @param accountName 账户名称
     * @return 交易记录列表
     */
    public List<TradeRecord> getTradeRecordsByAccountName(String accountName) {
        System.out.println("Fetching trade records for account: " + accountName);
        List<TradeRecordEntity> entities = tradeRecordRepository.findByAccountName(accountName);
        System.out.println("Found " + entities.size() + " entities");
        for (TradeRecordEntity entity : entities) {
            System.out.println("Entity: " + entity.getSymbol() + ", " + entity.getTradeType() + ", " + entity.getAccountName() + ", " + entity.getTradeTime());
        }
        List<TradeRecord> records = convertToModelList(entities);
        System.out.println("Converted to " + records.size() + " records");
        return records;
    }
    

}