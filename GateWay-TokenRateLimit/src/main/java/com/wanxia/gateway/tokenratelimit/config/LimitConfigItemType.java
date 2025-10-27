package com.wanxia.gateway.tokenratelimit.config;

/**
 * 限流配置项 key 类型枚举
 */
public enum LimitConfigItemType {
    /**
     * 精确匹配
     */
    EXACT("exact"),

    /**
     * 正则表达式匹配
     */
    REGEXP("regexp"),

    /**
     * 匹配所有情况
     */
    ALL("*"),

    /**
     * IP 段匹配
     */
    IP_NET("ipNet");

    private final String value;

    LimitConfigItemType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LimitConfigItemType fromValue(String value) {
        for (LimitConfigItemType type : LimitConfigItemType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}

