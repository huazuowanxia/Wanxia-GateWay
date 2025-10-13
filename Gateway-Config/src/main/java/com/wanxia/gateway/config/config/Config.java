package com.wanxia.gateway.config.config;

import com.wanxia.gateway.config.pojo.RouteDefinition;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关静态配置
 */
@Data
public class Config {

    // base
    private String name = DEFAULT_NAME; // 服务名称
    private int port = DEFAULT_PORT; // 端口
    private String env = DEFAULT_ENV; // 环境

    // 配置中心
    private ConfigCenter configCenter = new ConfigCenter();

    // 注册中心
    private RegisterCenter registerCenter = new RegisterCenter();

    // netty
    private NettyConfig netty = new NettyConfig();

    // http client
    private HttpClientConfig httpClient = new HttpClientConfig();

    // 路由配置
    private List<RouteDefinition> routes = new ArrayList<>();

}
