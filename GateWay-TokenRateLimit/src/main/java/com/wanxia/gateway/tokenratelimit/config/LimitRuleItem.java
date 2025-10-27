package com.wanxia.gateway.tokenratelimit.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 限流规则项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LimitRuleItem {

    /**
     * 限流类型
     */
    private LimitRuleItemType limitType;

    /**
     * 根据该 key 值进行限流
     * 对于 limit_by_consumer 和 limit_by_per_consumer 类型，该值为 ConsumerHeader
     * 对于其他类型，该值为对应的 key 值
     */
    private String key;

    /**
     * 按 IP 限流的配置
     */
    private LimitByPerIp limitByPerIp;

    /**
     * 限流配置项列表
     */
    private List<LimitConfigItem> configItems;
}

