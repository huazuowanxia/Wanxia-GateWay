package com.wanxia.gateway.tokenratelimit.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按 IP 限流的配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LimitByPerIp {

    /**
     * IP 来源类型（remote-addr 或 header）
     */
    private String sourceType;

    /**
     * 根据该请求头获取客户端 IP（仅当 sourceType 为 header 时使用）
     */
    private String headerName;
}

