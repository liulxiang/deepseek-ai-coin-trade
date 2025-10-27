package com.noodle.app.trade.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(name = "account_name", nullable = false)
    private String accountName;
    
    @Column(name = "balance", precision = 20, scale = 10)
    private BigDecimal balance;
    
    @Column(name = "total_value", precision = 20, scale = 10)
    private BigDecimal totalValue;
    
    // Constructors
    public AccountEntity() {}
    
    public AccountEntity(String accountName, BigDecimal balance) {
        this.accountName = accountName;
        this.balance = balance;
        this.totalValue = balance; // 初始化总价值为余额
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
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
}