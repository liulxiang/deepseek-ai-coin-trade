package com.noodle.app.trade.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    
    // 北京时区
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");
    
    // 日期时间格式化器
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 获取当前北京时间
     * @return 北京时间
     */
    public static LocalDateTime getBeijingTime() {
        return LocalDateTime.now(BEIJING_ZONE);
    }
    
    /**
     * 将指定时间转换为北京时间
     * @param dateTime 指定时间
     * @return 北京时间
     */
    public static LocalDateTime toBeijingTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(BEIJING_ZONE).toLocalDateTime();
    }
    
    /**
     * 格式化北京时间
     * @param dateTime 时间
     * @return 格式化后的字符串
     */
    public static String formatBeijingTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }
    
    /**
     * 获取格式化的当前北京时间
     * @return 格式化后的北京时间字符串
     */
    public static String getCurrentBeijingTimeFormatted() {
        return getBeijingTime().format(DEFAULT_FORMATTER);
    }
}