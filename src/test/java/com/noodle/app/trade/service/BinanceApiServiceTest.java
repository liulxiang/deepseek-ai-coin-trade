package com.noodle.app.trade.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BinanceApiServiceTest {

    @Autowired
    private BinanceApiService binanceApiService;

    @Test
    public void testGetCurrentPrice() {
        try {
            // 测试获取BTC价格
            assertNotNull(binanceApiService.getCurrentPrice("BTCUSDT"));
            // 测试获取ETH价格
            assertNotNull(binanceApiService.getCurrentPrice("ETHUSDT"));
        } catch (IOException e) {
            fail("获取价格失败: " + e.getMessage());
        }
    }

    @Test
    public void testGetAccountInfo() {
        try {
            // 测试获取账户信息（需要配置API密钥）
            String accountInfo = binanceApiService.getAccountInfo();
            // 如果没有配置API密钥，应该抛出异常
            if (accountInfo.contains("API密钥未配置")) {
                System.out.println("Binance API密钥未配置，跳过账户信息测试");
            } else {
                assertNotNull(accountInfo);
            }
        } catch (IOException e) {
            // 如果API密钥未配置，这是预期的行为
            if (e.getMessage().contains("API密钥未配置")) {
                System.out.println("Binance API密钥未配置，跳过账户信息测试");
            } else {
                fail("获取账户信息失败: " + e.getMessage());
            }
        }
    }
}