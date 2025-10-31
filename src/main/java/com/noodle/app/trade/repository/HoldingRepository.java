package com.noodle.app.trade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.noodle.app.trade.entity.HoldingEntity;

@Repository
public interface HoldingRepository extends JpaRepository<HoldingEntity, Long> {
    
    /**
     * 根据账户ID和币种符号查找持仓
     * @param accountId 账户ID
     * @param symbol 币种符号
     * @return 持仓实体
     */
    Optional<HoldingEntity> findByAccountIdAndSymbol(Long accountId, String symbol);
    
    /**
     * 根据账户ID查找所有持仓
     * @param accountId 账户ID
     * @return 持仓实体列表
     */
    List<HoldingEntity> findByAccountId(Long accountId);
}