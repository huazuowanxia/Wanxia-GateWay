package com.wanxia.gateway.core.filter.gray.strategy;

import com.wanxia.gateway.config.pojo.ServiceInstance;
import com.wanxia.gateway.core.context.GatewayContext;

import java.util.List;

public interface GrayStrategy {

    boolean shouldRoute2Gray(GatewayContext context, List<ServiceInstance> instances);

    String mark();

}
