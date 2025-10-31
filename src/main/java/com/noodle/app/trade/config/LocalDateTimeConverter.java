package com.noodle.app.trade.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Long> {
    
    // 使用北京时间时区
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public Long convertToDatabaseColumn(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        // 将LocalDateTime转换为毫秒时间戳存储到数据库，使用北京时间
        return localDateTime.atZone(BEIJING_ZONE).toInstant().toEpochMilli();
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        // 如果时间戳是毫秒格式，直接使用；如果是秒格式，转换为毫秒
        if (timestamp < 10000000000L) {
            timestamp = timestamp * 1000; // 转换为毫秒
        }
        // 将毫秒时间戳转换为LocalDateTime，使用北京时间
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), BEIJING_ZONE);
    }
}