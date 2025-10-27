package com.noodle.app.trade.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BinanceApiServiceTest {

    @Autowired
    private BinanceApiService binanceApiService;

    @Test
    public void testGetCurrentPrice() throws IOException {
        // 测试获取DOGEUSDT的价格
        BigDecimal dogePrice = binanceApiService.getCurrentPrice("DOGEUSDT");
        assertNotNull(dogePrice, "DOGEUSDT价格不应为null");
        assertTrue(dogePrice.compareTo(BigDecimal.ZERO) > 0, "DOGEUSDT价格应大于0");
        System.out.println("DOGEUSDT当前价格: " + dogePrice);

        // 测试获取BTCUSDT的价格
        BigDecimal btcPrice = binanceApiService.getCurrentPrice("BTCUSDT");
        assertNotNull(btcPrice, "BTCUSDT价格不应为null");
        assertTrue(btcPrice.compareTo(BigDecimal.ZERO) > 0, "BTCUSDT价格应大于0");
        System.out.println("BTCUSDT当前价格: " + btcPrice);
    }
}