package com.wanxia.gateway.core.filter.loadbalance.strategy;

import com.wanxia.gateway.config.pojo.ServiceInstance;
import com.wanxia.gateway.core.context.GatewayContext;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.wanxia.gateway.common.constant.LoadBalanceConstant.RANDOM_LOAD_BALANCE_STRATEGY;

public class RandomLoadBalanceStrategy implements LoadBalanceStrategy {

    @Override
    public ServiceInstance selectInstance(GatewayContext context, List<ServiceInstance> instances) {
        return instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
    }

    @Override
    public String mark() {
        return RANDOM_LOAD_BALANCE_STRATEGY;
    }

}
