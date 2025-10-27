package com.noodle.app.trade.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trading")
public class TradingViewController {
    
    /**
     * 显示交易监控页面
     * @return 页面视图名称
     */
    @GetMapping("/dashboard")
    public String showTradingDashboard() {
        return "trading-dashboard";
    }
}