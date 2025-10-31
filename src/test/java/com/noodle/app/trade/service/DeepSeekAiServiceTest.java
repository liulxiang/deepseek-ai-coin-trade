package com.noodle.app.trade.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.noodle.app.trade.model.CryptoCurrency;

@SpringBootTest
public class DeepSeekAiServiceTest {

    @Test
    public void testAnalyzeMarketData() throws Exception {
        DeepSeekAiService aiService = new DeepSeekAiService();
        
        // 模拟市场数据
        List<CryptoCurrency> cryptoCurrencies = Arrays.asList(
            createCryptoCurrency("BTC", "Bitcoin", new BigDecimal("45000.00"), 
                               new BigDecimal("500.00"), new BigDecimal("1.12"), 
                               new BigDecimal("2500000000.00")),
            createCryptoCurrency("ETH", "Ethereum", new BigDecimal("3000.00"), 
                               new BigDecimal("-50.00"), new BigDecimal("-1.64"), 
                               new BigDecimal("1500000000.00")),
            createCryptoCurrency("DOGE", "Dogecoin", new BigDecimal("0.15"), 
                               new BigDecimal("0.01"), new BigDecimal("7.14"), 
                               new BigDecimal("500000000.00"))
        );
        
        // 模拟账户持仓
        Map<String, Double> accountHoldings = new HashMap<>();
        accountHoldings.put("BTC", 0.5);  // 持有0.5个BTC
        accountHoldings.put("ETH", 2.0);   // 持有2.0个ETH
        // DOGE没有持仓
        
        // 模拟可用余额
        double availableBalance = 10000.00; // 10000美元可用资金
        
        // 模拟市场情况
        String marketConditions = "震荡市，整体市场情绪谨慎";
        
        // 调用优化后的analyzeMarketData方法
        String result = aiService.analyzeMarketData(cryptoCurrencies, accountHoldings, availableBalance, marketConditions);
        
        System.out.println("AI分析结果：");
        System.out.println(result);
    }
    
    @Test
    public void testGenerateQuickTradingSignals() throws Exception {
        DeepSeekAiService aiService = new DeepSeekAiService();
        
        // 模拟市场数据
        List<CryptoCurrency> cryptoCurrencies = Arrays.asList(
            createCryptoCurrency("BTC", "Bitcoin", new BigDecimal("45200.00"), 
                               new BigDecimal("300.00"), new BigDecimal("0.67"), 
                               new BigDecimal("2000000000.00")),
            createCryptoCurrency("ETH", "Ethereum", new BigDecimal("3050.00"), 
                               new BigDecimal("25.00"), new BigDecimal("0.83"), 
                               new BigDecimal("1200000000.00")),
            createCryptoCurrency("ADA", "Cardano", new BigDecimal("0.45"), 
                               new BigDecimal("-0.02"), new BigDecimal("-4.26"), 
                               new BigDecimal("300000000.00"))
        );
        
        // 模拟账户持仓
        Map<String, Double> accountHoldings = new HashMap<>();
        accountHoldings.put("BTC", 0.3);
        accountHoldings.put("ADA", 1000.0);
        
        // 模拟可用余额
        double availableBalance = 5000.00;
        
        // 调用快速交易信号生成方法
        Map<String, String> signals = aiService.generateQuickTradingSignals(cryptoCurrencies, accountHoldings, availableBalance);
        
        System.out.println("快速交易信号：");
        for (Map.Entry<String, String> entry : signals.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
    
    @Test
    public void testGetDeepSeekAiBalance() throws Exception {
        DeepSeekAiService aiService = new DeepSeekAiService();
        
        // 测试获取DeepSeek API余额
        String balanceInfo = aiService.getDeepSeekAiBalance();
        
        System.out.println("DeepSeek API余额信息：");
        System.out.println(balanceInfo);
        
        // 验证返回结果不为空
        assertNotNull(balanceInfo);
        assertTrue(balanceInfo.contains("余额") || balanceInfo.contains("balance") || balanceInfo.contains("error"));
    }
    
    private CryptoCurrency createCryptoCurrency(String symbol, String name, BigDecimal price, 
                                              BigDecimal priceChange, BigDecimal priceChangePercent, 
                                              BigDecimal volume) {
        CryptoCurrency crypto = new CryptoCurrency();
        crypto.setSymbol(symbol);
        crypto.setName(name);
        crypto.setPrice(price);
        crypto.setPriceChange(priceChange);
        crypto.setPriceChangePercent(priceChangePercent);
        crypto.setVolume(volume);
        crypto.setLastUpdated(LocalDateTime.now());
        return crypto;
    }
}