package com.noodle.app.trade.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 主控制器 - 提供服务器总体状态信息
 */
@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    private ApplicationContext applicationContext;

  
}