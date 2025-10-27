package com.noodle.app.trade.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_value_history")
public class AccountValueHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(name = "account_name", nullable = false)
    private String accountName;
    
    @Column(name = "total_value", precision = 20, scale = 10)
    private BigDecimal totalValue;
    
    @Column(name = "record_time")
    private LocalDateTime recordTime;
    
    // Constructors
    public AccountValueHistory() {}
    
    public AccountValueHistory(String accountName, BigDecimal totalValue, LocalDateTime recordTime) {
        this.accountName = accountName;
        this.totalValue = totalValue;
        this.recordTime = recordTime;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
    
    public LocalDateTime getRecordTime() {
        return recordTime;
    }
    
    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }
}