package com.wanxia.gateway.tokenratelimit.config;

/**
 * 限流规则项类型枚举
 */
public enum LimitRuleItemType {
    /**
     * 按请求头限流
     */
    LIMIT_BY_HEADER("limit_by_header"),

    /**
     * 按请求参数限流
     */
    LIMIT_BY_PARAM("limit_by_param"),

    /**
     * 按消费者限流
     */
    LIMIT_BY_CONSUMER("limit_by_consumer"),

    /**
     * 按 Cookie 限流
     */
    LIMIT_BY_COOKIE("limit_by_cookie"),

    /**
     * 按请求头值限流（每个值单独限流）
     */
    LIMIT_BY_PER_HEADER("limit_by_per_header"),

    /**
     * 按请求参数值限流（每个值单独限流）
     */
    LIMIT_BY_PER_PARAM("limit_by_per_param"),

    /**
     * 按消费者限流（每个消费者单独限流）
     */
    LIMIT_BY_PER_CONSUMER("limit_by_per_consumer"),

    /**
     * 按 Cookie 值限流（每个值单独限流）
     */
    LIMIT_BY_PER_COOKIE("limit_by_per_cookie"),

    /**
     * 按 IP 限流（每个 IP 单独限流）
     */
    LIMIT_BY_PER_IP("limit_by_per_ip");

    private final String value;

    LimitRuleItemType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LimitRuleItemType fromValue(String value) {
        for (LimitRuleItemType type : LimitRuleItemType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}

