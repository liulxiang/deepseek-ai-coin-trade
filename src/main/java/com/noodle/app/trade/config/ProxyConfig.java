package com.noodle.app.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {
    
    private String httpHost;
    private Integer httpPort;
    private String httpsHost;
    private Integer httpsPort;
    private String username;
    private String password;
    
    // Getters and Setters
    
    public String getHttpHost() {
        return httpHost;
    }
    
    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }
    
    public Integer getHttpPort() {
        return httpPort;
    }
    
    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }
    
    public String getHttpsHost() {
        return httpsHost;
    }
    
    public void setHttpsHost(String httpsHost) {
        this.httpsHost = httpsHost;
    }
    
    public Integer getHttpsPort() {
        return httpsPort;
    }
    
    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * 检查是否配置了HTTP代理
     * @return 是否配置了HTTP代理
     */
    public boolean isHttpProxyConfigured() {
        return httpHost != null && !httpHost.isEmpty() && httpPort != null;
    }
    
    /**
     * 检查是否配置了HTTPS代理
     * @return 是否配置了HTTPS代理
     */
    public boolean isHttpsProxyConfigured() {
        return httpsHost != null && !httpsHost.isEmpty() && httpsPort != null;
    }
}