package com.noodle.app.trade.repository;

import com.noodle.app.trade.entity.TradeRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRecordRepository extends JpaRepository<TradeRecordEntity, Long> {
    
    /**
     * 根据账户名称查找交易记录
     * @param accountName 账户名称
     * @return 交易记录列表
     */
    List<TradeRecordEntity> findByAccountName(String accountName);
    
    /**
     * 根据币种符号查找交易记录
     * @param symbol 币种符号
     * @return 交易记录列表
     */
    List<TradeRecordEntity> findBySymbol(String symbol);
    
    /**
     * 根据交易类型查找交易记录
     * @param tradeType 交易类型 (BUY/SELL)
     * @return 交易记录列表
     */
    List<TradeRecordEntity> findByTradeType(String tradeType);
    
    /**
     * 根据交易时间范围查找交易记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    List<TradeRecordEntity> findByTradeTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据币种符号和交易时间范围查找交易记录
     * @param symbol 币种符号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    List<TradeRecordEntity> findBySymbolAndTradeTimeBetween(String symbol, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找最新的交易记录
     * @param limit 数量限制
     * @return 交易记录列表
     */
    @Query("SELECT t FROM TradeRecordEntity t ORDER BY t.tradeTime DESC")
    List<TradeRecordEntity> findLatestTrades(int limit);
    
    /**
     * 根据交易策略查找交易记录
     * @param strategy 交易策略
     * @return 交易记录列表
     */
    List<TradeRecordEntity> findByStrategy(String strategy);
}