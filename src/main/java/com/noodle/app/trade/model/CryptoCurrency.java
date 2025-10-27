package com.noodle.app.trade.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CryptoCurrency {
    private Long id;
    private String symbol; // 币种符号，如 BTC, ETH 等
    private String name;   // 币种名称
    private BigDecimal price; // 当前价格
    private BigDecimal priceChange; // 价格变化
    private BigDecimal priceChangePercent; // 价格变化百分比
    private BigDecimal volume; // 交易量
    private LocalDateTime lastUpdated; // 最后更新时间
    
    // 构造函数
    public CryptoCurrency() {}
    
    public CryptoCurrency(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }
    
    public CryptoCurrency(String symbol, String name, BigDecimal price, 
                         BigDecimal priceChange, BigDecimal priceChangePercent, 
                         BigDecimal volume, LocalDateTime lastUpdated) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.priceChange = priceChange;
        this.priceChangePercent = priceChangePercent;
        this.volume = volume;
        this.lastUpdated = lastUpdated;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getPriceChange() {
        return priceChange;
    }
    
    public void setPriceChange(BigDecimal priceChange) {
        this.priceChange = priceChange;
    }
    
    public BigDecimal getPriceChangePercent() {
        return priceChangePercent;
    }
    
    public void setPriceChangePercent(BigDecimal priceChangePercent) {
        this.priceChangePercent = priceChangePercent;
    }
    
    public BigDecimal getVolume() {
        return volume;
    }
    
    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}