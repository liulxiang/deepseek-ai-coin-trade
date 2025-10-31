package com.noodle.app.trade.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.noodle.app.trade.config.LocalDateTimeConverter;

@Entity
@Table(name = "trade_record")
public class TradeRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(name = "symbol", nullable = false)
    private String symbol;
    
    @Column(name = "trade_type", nullable = false)
    private String tradeType;
    
    @Column(name = "price", precision = 20, scale = 10)
    private BigDecimal price;
    
    @Column(name = "quantity", precision = 20, scale = 10)
    private BigDecimal quantity;
    
    @Column(name = "amount", precision = 20, scale = 10)
    private BigDecimal amount;
    
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "trade_time", nullable = false)
    private LocalDateTime tradeTime;
    
    @Column(name = "strategy")
    private String strategy;
    
    @Column(name = "account_name")
    private String accountName;
    
    // Constructors
    public TradeRecordEntity() {}
    
    public TradeRecordEntity(String symbol, String tradeType, BigDecimal price, 
                            BigDecimal quantity, BigDecimal amount, LocalDateTime tradeTime, String strategy) {
        this.symbol = symbol;
        this.tradeType = tradeType;
        this.price = price;
        this.quantity = quantity;
        this.amount = amount;
        this.tradeTime = tradeTime;
        this.strategy = strategy;
    }
    
    public TradeRecordEntity(String symbol, String tradeType, BigDecimal price, 
                            BigDecimal quantity, BigDecimal amount, LocalDateTime tradeTime, String strategy, String accountName) {
        this.symbol = symbol;
        this.tradeType = tradeType;
        this.price = price;
        this.quantity = quantity;
        this.amount = amount;
        this.tradeTime = tradeTime;
        this.strategy = strategy;
        this.accountName = accountName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getTradeType() {
        return tradeType;
    }
    
    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDateTime getTradeTime() {
        return tradeTime;
    }
    
    public void setTradeTime(LocalDateTime tradeTime) {
        this.tradeTime = tradeTime;
    }
    
    public String getStrategy() {
        return strategy;
    }
    
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}