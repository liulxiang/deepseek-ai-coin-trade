package com.noodle.app.trade.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.noodle.app.trade.entity.AccountEntity;
import com.noodle.app.trade.entity.HoldingEntity;
import com.noodle.app.trade.entity.TradeRecordEntity;
import com.noodle.app.trade.model.Account;
import com.noodle.app.trade.model.CryptoCurrency;
import com.noodle.app.trade.model.TradeRecord;
import com.noodle.app.trade.repository.AccountRepository;
import com.noodle.app.trade.repository.HoldingRepository;
import com.noodle.app.trade.repository.TradeRecordRepository;

@Service
public class TradingService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TradeRecordRepository tradeRecordRepository;
    
    @Autowired
    private HoldingRepository holdingRepository;
    
    /**
     * 创建模拟账户
     * @param accountName 账户名称
     * @param initialBalance 初始余额
     * @return 账户对象
     */
    public Account createAccount(String accountName, BigDecimal initialBalance) {
        // 检查账户是否已存在
        Optional<AccountEntity> existingAccount = accountRepository.findByAccountName(accountName);
        if (existingAccount.isPresent()) {
            throw new RuntimeException("账户已存在: " + accountName);
        }
        
        // 创建新账户
        AccountEntity accountEntity = new AccountEntity(accountName, initialBalance);
        accountEntity = accountRepository.save(accountEntity);
        
        // 转换为模型对象
        Account account = new Account(accountName, initialBalance);
        account.setId(accountEntity.getId());
        
        return account;
    }
    
    /**
     * 重置模拟账户
     * @param accountName 账户名称
     * @param initialBalance 初始余额
     * @return 重置后的账户对象
     */
    public Account resetAccount(String accountName, BigDecimal initialBalance) {
        // 查找现有账户
        Optional<AccountEntity> existingAccount = accountRepository.findByAccountName(accountName);
        
        AccountEntity accountEntity;
        if (existingAccount.isPresent()) {
            // 如果账户存在，重置余额和总价值
            accountEntity = existingAccount.get();
            accountEntity.setBalance(initialBalance);
            accountEntity.setTotalValue(initialBalance);
            accountEntity = accountRepository.save(accountEntity);
            
            // 删除该账户的所有持仓
            if (accountEntity.getId() != null) {
                List<HoldingEntity> holdings = holdingRepository.findByAccountId(accountEntity.getId());
                if (holdings != null && !holdings.isEmpty()) {
                    holdingRepository.deleteAll(holdings);
                }
            }
            
            // 注意：由于交易记录没有直接关联账户的字段，我们暂时不删除交易记录
        } else {
            // 如果账户不存在，创建新账户
            accountEntity = new AccountEntity(accountName, initialBalance);
            accountEntity = accountRepository.save(accountEntity);
        }
        
        // 检查accountEntity是否为null
        if (accountEntity == null) {
            throw new RuntimeException("账户实体保存失败");
        }
        
        // 转换为模型对象
        Account account = new Account(accountName, initialBalance);
        account.setId(accountEntity.getId());
        
        return account;
    }
    
    /**
     * 获取账户信息
     * @param accountName 账户名称
     * @return 账户对象
     */
    public Account getAccount(String accountName) {
        System.out.println("Getting account: " + accountName);
        Optional<AccountEntity> accountEntityOptional = accountRepository.findByAccountName(accountName);
        System.out.println("Account entity optional present: " + accountEntityOptional.isPresent());
        
        if (!accountEntityOptional.isPresent()) {
            throw new RuntimeException("账户不存在: " + accountName);
        }
        
        AccountEntity accountEntity = accountEntityOptional.get();
        System.out.println("Account entity ID: " + accountEntity.getId());
        System.out.println("Account entity name: " + accountEntity.getAccountName());
        System.out.println("Account entity balance: " + accountEntity.getBalance());
        
        Account account = new Account(accountEntity.getAccountName(), accountEntity.getBalance());
        account.setId(accountEntity.getId());
        
        // 获取持仓信息
        if (accountEntity.getId() != null) {
            System.out.println("Fetching holdings for account ID: " + accountEntity.getId());
            List<HoldingEntity> holdings = holdingRepository.findByAccountId(accountEntity.getId());
            System.out.println("Holdings count: " + holdings.size());
            
            for (HoldingEntity holding : holdings) {
                if (holding != null && holding.getSymbol() != null && holding.getQuantity() != null) {
                    System.out.println("Adding holding: " + holding.getSymbol() + " = " + holding.getQuantity());
                    account.addHolding(holding.getSymbol(), holding.getQuantity());
                    System.out.println("Holding added successfully");
                } else {
                    System.out.println("Skipping invalid holding: " + holding);
                }
            }
            
            account.setHoldingList(holdings);
        }
        
        System.out.println("Account creation completed");
        return account;
    }
    
    /**
     * 执行买入交易
     * @param accountName 账户名称
     * @param cryptoCurrency 加密货币信息
     * @param quantity 购买数量
     * @param strategy 使用的交易策略
     * @return 交易记录
     */
    public TradeRecord executeBuy(String accountName, CryptoCurrency cryptoCurrency, BigDecimal quantity, String strategy) {
        // 获取账户信息
        Account account = getAccount(accountName);
        
        // 计算交易金额
        BigDecimal price = cryptoCurrency.getPrice();
        BigDecimal amount = price.multiply(quantity);
        
        // 检查余额是否足够
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足，无法完成交易");
        }
        
        // 更新账户余额
        AccountEntity accountEntity = accountRepository.findByAccountName(accountName).get();
        accountEntity.setBalance(accountEntity.getBalance().subtract(amount));
        accountRepository.save(accountEntity);
        
        // 更新持仓
        updateHoldingAdd(accountEntity.getId(), cryptoCurrency.getSymbol(), quantity,amount);
        
        // 记录交易
        TradeRecordEntity tradeRecordEntity = new TradeRecordEntity(
                cryptoCurrency.getSymbol(),
                "BUY",
                price,
                quantity,
                amount,
                LocalDateTime.now(),
                strategy,
                accountName
        );
        tradeRecordEntity = tradeRecordRepository.save(tradeRecordEntity);
        
        // 转换为模型对象
        TradeRecord tradeRecord = new TradeRecord(
                cryptoCurrency.getSymbol(),
                "BUY",
                price,
                quantity,
                amount,
                tradeRecordEntity.getTradeTime(),
                strategy
        );
        tradeRecord.setId(tradeRecordEntity.getId());
        
        return tradeRecord;
    }
    
    /**
     * 执行卖出交易
     * @param accountName 账户名称
     * @param cryptoCurrency 加密货币信息
     * @param quantity 卖出数量
     * @param strategy 使用的交易策略
     * @return 交易记录
     */
    public TradeRecord executeSell(String accountName, CryptoCurrency cryptoCurrency, BigDecimal quantity, String strategy) {
        // 获取账户信息
        Account account = getAccount(accountName);
        
        // 检查持仓是否足够
        BigDecimal holdingQuantity = account.getHolding(cryptoCurrency.getSymbol());
        if (holdingQuantity.compareTo(quantity) < 0) {
            throw new RuntimeException("持仓不足，无法完成交易");
        }
        
        // 计算交易金额
        BigDecimal price = cryptoCurrency.getPrice();
        BigDecimal amount = price.multiply(quantity);
        
        // 更新账户余额
        AccountEntity accountEntity = accountRepository.findByAccountName(accountName).get();
        accountEntity.setBalance(accountEntity.getBalance().add(amount));
        accountRepository.save(accountEntity);
        
        // 更新持仓
        updateHoldingReduce(accountEntity.getId(), cryptoCurrency.getSymbol(), quantity,amount);
        
        // 记录交易
        TradeRecordEntity tradeRecordEntity = new TradeRecordEntity(
                cryptoCurrency.getSymbol(),
                "SELL",
                price,
                quantity,
                amount,
                LocalDateTime.now(),
                strategy,
                accountName
        );
        tradeRecordEntity = tradeRecordRepository.save(tradeRecordEntity);
        
        // 转换为模型对象
        TradeRecord tradeRecord = new TradeRecord(
                cryptoCurrency.getSymbol(),
                "SELL",
                price,
                quantity,
                amount,
                tradeRecordEntity.getTradeTime(),
                strategy
        );
        tradeRecord.setId(tradeRecordEntity.getId());
        
        return tradeRecord;
    }
    
    /**
     * 更新持仓
     * @param accountId 账户ID
     * @param symbol 币种符号
     * @param quantity 变化数量（正数表示增加，负数表示减少）
     */
    private void updateHoldingAdd(Long accountId, String symbol, BigDecimal quantity,BigDecimal holdCost) {
        Optional<HoldingEntity> holdingOptional = holdingRepository.findByAccountIdAndSymbol(accountId, symbol);
        
        if (holdingOptional.isPresent()) {
            // 更新现有持仓
            HoldingEntity holding = holdingOptional.get();
            BigDecimal newQuantity = holding.getQuantity().add(quantity);
            BigDecimal newHoldCost = holding.getHoldCost().add(holdCost);
            
            if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                // 如果持仓为0或负数，则删除持仓记录
                holdingRepository.delete(holding);
            } else {
                // 更新持仓数量
                holding.setQuantity(newQuantity);
                holding.setHoldCost(newHoldCost);
                holdingRepository.save(holding);
            }
        } else {
            // 创建新持仓
            if (quantity.compareTo(BigDecimal.ZERO) > 0) {
                HoldingEntity holding = new HoldingEntity(accountId, symbol, quantity);
                holding.setHoldCost(holdCost);
                holdingRepository.save(holding);
            }
        }
    }
    /**
     * 更新持仓
     * @param accountId 账户ID
     * @param symbol 币种符号
     * @param quantity 变化数量（正数表示增加，负数表示减少）
     */
    private void updateHoldingReduce(Long accountId, String symbol, BigDecimal quantity,BigDecimal holdCost) {
    	Optional<HoldingEntity> holdingOptional = holdingRepository.findByAccountIdAndSymbol(accountId, symbol);
    	
    	if (holdingOptional.isPresent()) {
    		// 更新现有持仓
    		HoldingEntity holding = holdingOptional.get();
    		BigDecimal newQuantity = holding.getQuantity().subtract(quantity);
    		BigDecimal newHoldCost = holding.getHoldCost().subtract(holdCost);
    		
    		if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
    			// 如果持仓为0或负数，则删除持仓记录
    			holdingRepository.delete(holding);
    		} else {
    			// 更新持仓数量
    			holding.setQuantity(newQuantity);
    			holding.setHoldCost(newHoldCost);
    			holdingRepository.save(holding);
    		}
    	} 
    }
    
    /**
     * 删除账户
     * @param accountName 账户名称
     * @return 是否删除成功
     */
    public boolean deleteAccount(String accountName) {
        // 查找账户
        Optional<AccountEntity> accountEntityOptional = accountRepository.findByAccountName(accountName);
        
        if (!accountEntityOptional.isPresent()) {
            return false; // 账户不存在
        }
        
        AccountEntity accountEntity = accountEntityOptional.get();
        
        // 删除该账户的所有持仓
        if (accountEntity.getId() != null) {
            List<HoldingEntity> holdings = holdingRepository.findByAccountId(accountEntity.getId());
            if (holdings != null && !holdings.isEmpty()) {
                holdingRepository.deleteAll(holdings);
            }
            
            // 删除该账户的所有交易记录
            List<TradeRecordEntity> tradeRecords = tradeRecordRepository.findByAccountName(accountName);
            if (tradeRecords != null && !tradeRecords.isEmpty()) {
                tradeRecordRepository.deleteAll(tradeRecords);
            }
        }
        
        // 删除账户
        accountRepository.delete(accountEntity);
        
        return true;
    }
    
    /**
     * 获取账户余额
     * @param accountName 账户名称
     * @return 账户余额
     */
    public BigDecimal getAccountBalance(String accountName) {
        Account account = getAccount(accountName);
        return account.getBalance();
    }
    
    /**
     * 获取账户持仓
     * @param accountName 账户名称
     * @param symbol 币种符号
     * @return 持仓数量
     */
    public BigDecimal getAccountHolding(String accountName, String symbol) {
        Account account = getAccount(accountName);
        return account.getHolding(symbol);
    }
}