package com.noodle.app.trade.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.noodle.app.trade.entity.CryptoCurrencyEntity;

@Repository
public interface CryptoCurrencyRepository extends JpaRepository<CryptoCurrencyEntity, Long> {
    
    /**
     * 根据币种符号查找所有加密货币记录
     * @param symbol 币种符号
     * @return 加密货币实体列表
     */
    List<CryptoCurrencyEntity> findBySymbol(String symbol);
    
    /**
     * 根据币种符号查找所有加密货币记录，按更新时间降序排列
     * @param symbol 币种符号
     * @return 加密货币实体列表
     */
    List<CryptoCurrencyEntity> findBySymbolOrderByLastUpdatedDesc(String symbol);
    
    /**
     * 根据币种符号和更新时间范围查找加密货币记录，按更新时间升序排列
     * @param symbol 币种符号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 加密货币实体列表
     */
    List<CryptoCurrencyEntity> findBySymbolAndLastUpdatedBetweenOrderByLastUpdatedAsc(String symbol, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据币种符号和更新时间范围查找加密货币记录
     * @param symbol 币种符号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 加密货币实体列表
     */
    List<CryptoCurrencyEntity> findBySymbolAndLastUpdatedBetween(String symbol, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找指定时间范围内的所有加密货币记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 加密货币实体列表
     */
    List<CryptoCurrencyEntity> findByLastUpdatedBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 删除指定时间之前的所有加密货币记录
     * @param dateTime 时间
     */
    void deleteByLastUpdatedBefore(LocalDateTime dateTime);
}