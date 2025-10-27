package com.wanxia.gateway.tokenratelimit.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI Token 限流配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiTokenRateLimitConfig {

    /**
     * 限流规则名称
     */
    private String ruleName;

    /**
     * 全局限流配置
     */
    private GlobalThreshold globalThreshold;

    /**
     * 限流规则项列表
     */
    private List<LimitRuleItem> ruleItems;

    /**
     * 当请求超过阈值被拒绝时，返回的 HTTP 状态码
     */
    private int rejectedCode;

    /**
     * 当请求超过阈值被拒绝时，返回的响应体
     */
    private String rejectedMsg;

    /**
     * Redis 客户端（用于存储限流计数）
     */
    private Object redisClient;

    /**
     * 计数器指标 Map
     */
    private Map<String, Object> counterMetrics;

    /**
     * 增加计数器
     *
     * @param metricName 指标名称
     * @param inc        增加的数量
     */
    public void incrementCounter(String metricName, long inc) {
        if (inc == 0) {
            return;
        }
        if (counterMetrics == null) {
            return;
        }
        Object counter = counterMetrics.get(metricName);
        if (counter == null) {
            // 这里需要根据实际的 Metrics 库来实现
            // 暂时留作占位符
            return;
        }
        // 调用 counter.increment(inc)
    }
}

