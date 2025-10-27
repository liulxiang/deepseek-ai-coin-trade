package com.noodle.app.trade.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TradeRecord {
    private Long id;
    private String symbol; // 交易币种
    private String tradeType; // 交易类型：BUY 或 SELL
    private BigDecimal price; // 交易价格
    private BigDecimal quantity; // 交易数量
    private BigDecimal amount; // 交易金额
    private LocalDateTime tradeTime; // 交易时间
    private String strategy; // 使用的交易策略
    
    // 构造函数
    public TradeRecord() {}
    
    public TradeRecord(String symbol, String tradeType, BigDecimal price, 
                      BigDecimal quantity, BigDecimal amount, LocalDateTime tradeTime, String strategy) {
        this.symbol = symbol;
        this.tradeType = tradeType;
        this.price = price;
        this.quantity = quantity;
        this.amount = amount;
        this.tradeTime = tradeTime;
        this.strategy = strategy;
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
}