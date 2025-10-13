package com.wanxia.gateway.register.service;

import com.wanxia.gateway.config.config.Config;

/**
 * 注册中心接口
 */
public interface RegisterCenterProcessor {

    /**
     * 注册中心初始化
     */
    void init(Config config);

    /**
     * 订阅注册中心实例变化
     */
    void subscribeServiceChange(RegisterCenterListener listener);

}
