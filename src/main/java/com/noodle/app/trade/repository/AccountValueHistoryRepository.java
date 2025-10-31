package com.noodle.app.trade.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.noodle.app.trade.entity.AccountValueHistory;

@Repository
public interface AccountValueHistoryRepository extends JpaRepository<AccountValueHistory, Long> {
    
    /**
     * 根据账户名称查找账户价值历史记录
     * @param accountName 账户名称
     * @return 账户价值历史记录列表
     */
    List<AccountValueHistory> findByAccountNameOrderByRecordTimeDesc(String accountName);
    
    /**
     * 根据账户名称和时间范围查找账户价值历史记录
     * @param accountName 账户名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 账户价值历史记录列表
     */
    List<AccountValueHistory> findByAccountNameAndRecordTimeBetweenOrderByRecordTimeAsc(String accountName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 删除指定时间之前的所有账户价值历史记录
     * @param dateTime 时间
     */
    void deleteByRecordTimeBefore(LocalDateTime dateTime);
}