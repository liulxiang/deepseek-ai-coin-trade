package com.noodle.app.trade.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProxyConfigTest {

    @Autowired
    private ProxyConfig proxyConfig;

    @Test
    public void testProxyConfigLoaded() {
        assertNotNull(proxyConfig, "ProxyConfig should be loaded");
    }

    @Test
    public void testIsHttpProxyConfigured() {
        // 默认情况下，代理应该未配置
        // 但测试环境中可能有环境变量设置，所以我们检查是否正确加载了配置
        if (proxyConfig.getHttpHost() == null || proxyConfig.getHttpHost().isEmpty()) {
            assertFalse(proxyConfig.isHttpProxyConfigured(), "HTTP proxy should not be configured when host is empty");
        } else {
            assertTrue(proxyConfig.isHttpProxyConfigured(), "HTTP proxy should be configured when host is set");
        }
    }

    @Test
    public void testIsHttpsProxyConfigured() {
        // 默认情况下，代理应该未配置
        // 但测试环境中可能有环境变量设置，所以我们检查是否正确加载了配置
        if (proxyConfig.getHttpsHost() == null || proxyConfig.getHttpsHost().isEmpty()) {
            assertFalse(proxyConfig.isHttpsProxyConfigured(), "HTTPS proxy should not be configured when host is empty");
        } else {
            assertTrue(proxyConfig.isHttpsProxyConfigured(), "HTTPS proxy should be configured when host is set");
        }
    }
}