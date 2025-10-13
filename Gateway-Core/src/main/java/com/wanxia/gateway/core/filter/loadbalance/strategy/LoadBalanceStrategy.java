package com.wanxia.gateway.core.filter.loadbalance.strategy;

import com.wanxia.gateway.config.pojo.ServiceInstance;
import com.wanxia.gateway.core.context.GatewayContext;

import java.util.List;

public interface LoadBalanceStrategy {

    ServiceInstance selectInstance(GatewayContext context, List<ServiceInstance> instances);

    String mark();

}
