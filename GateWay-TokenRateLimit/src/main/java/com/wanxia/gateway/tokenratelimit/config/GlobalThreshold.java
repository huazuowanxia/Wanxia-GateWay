package com.wanxia.gateway.tokenratelimit.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局限流阈值配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalThreshold {

    /**
     * 时间窗口内的 token 数
     */
    private long count;

    /**
     * 时间窗口大小（秒）
     */
    private long timeWindow;
}

