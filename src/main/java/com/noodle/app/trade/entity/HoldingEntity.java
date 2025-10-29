package com.noodle.app.trade.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "holding")
public class HoldingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    
    @Column(name = "symbol", nullable = false)
    private String symbol;
    
    @Column(name = "quantity", precision = 20, scale = 10)
    private BigDecimal quantity;
    
    @Column(name = "hold_cost", precision = 20, scale = 10)
    private BigDecimal holdCost;
    
    // Constructors
    public HoldingEntity() {}
    
    public HoldingEntity(Long accountId, String symbol, BigDecimal quantity) {
        this.accountId = accountId;
        this.symbol = symbol;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

	public BigDecimal getHoldCost() {
		return holdCost;
	}

	public void setHoldCost(BigDecimal holdCost) {
		this.holdCost = holdCost;
	}
}