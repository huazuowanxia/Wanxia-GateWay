package com.wanxia.gateway.tokenratelimit.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * 限流配置项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LimitConfigItem {

    /**
     * 限流配置项 key 类型
     */
    private LimitConfigItemType configType;

    /**
     * 限流 key
     */
    private String key;

    /**
     * 限流 key 转换的 IP 地址或 IP 段（仅用于 configType 为 IP_NET）
     */
    private String ipNet;

    /**
     * 正则表达式（仅用于 configType 为 REGEXP）
     */
    private Pattern regexp;

    /**
     * 指定时间窗口内的 token 数
     */
    private long count;

    /**
     * 时间窗口大小（秒）
     */
    private long timeWindow;
}

