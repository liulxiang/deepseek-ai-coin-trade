package com.noodle.app.trade.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.noodle.app.trade.entity.AccountEntity;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    
    /**
     * 根据账户名称查找账户
     * @param accountName 账户名称
     * @return 账户实体
     */
    Optional<AccountEntity> findByAccountName(String accountName);
}