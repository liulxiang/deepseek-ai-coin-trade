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
@Table(name = "crypto_currency")
public class CryptoCurrencyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(name = "symbol", nullable = false)
    private String symbol;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "price", precision = 20, scale = 10)
    private BigDecimal price;
    
    @Column(name = "price_change", precision = 20, scale = 10)
    private BigDecimal priceChange;
    
    @Column(name = "price_change_percent", precision = 10, scale = 3)
    private BigDecimal priceChangePercent;
    
    @Column(name = "volume", precision = 20, scale = 10)
    private BigDecimal volume;
    
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    // Constructors
    public CryptoCurrencyEntity() {}
    
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