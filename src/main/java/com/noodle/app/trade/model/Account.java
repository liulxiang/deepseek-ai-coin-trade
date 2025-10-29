package com.noodle.app.trade.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.noodle.app.trade.entity.HoldingEntity;

public class Account {
    private Long id;
    private String accountName; // 账户名称
    private BigDecimal balance; // 账户余额
    private Map<String, BigDecimal> holdings; // 持仓信息，key为币种符号，value为持有数量
    private BigDecimal totalValue; // 账户总价值
    
    private  List<HoldingEntity> holdingList;
    // 构造函数
    public Account() {
        this.holdings = new HashMap<>();
        this.balance = BigDecimal.ZERO;
        this.totalValue = BigDecimal.ZERO;
    }
    
    public Account(String accountName, BigDecimal balance) {
        this();
        this.accountName = accountName;
        this.balance = balance;
    }
    
    // 添加持仓
    public void addHolding(String symbol, BigDecimal quantity) {
        holdings.put(symbol, holdings.getOrDefault(symbol, BigDecimal.ZERO).add(quantity));
    }
    
    // 减少持仓
    public void reduceHolding(String symbol, BigDecimal quantity) {
        if (holdings.containsKey(symbol)) {
            BigDecimal currentQuantity = holdings.get(symbol);
            BigDecimal newQuantity = currentQuantity.subtract(quantity);
            if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                holdings.remove(symbol);
            } else {
                holdings.put(symbol, newQuantity);
            }
        }
    }
    
    // 获取持仓数量
    public BigDecimal getHolding(String symbol) {
        return holdings.getOrDefault(symbol, BigDecimal.ZERO);
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
    
    public Map<String, BigDecimal> getHoldings() {
        return holdings;
    }
    
    public void setHoldings(Map<String, BigDecimal> holdings) {
        this.holdings = holdings;
    }
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

	public List<HoldingEntity> getHoldingList() {
		return holdingList;
	}

	public void setHoldingList(List<HoldingEntity> holdingList) {
		this.holdingList = holdingList;
	}
}