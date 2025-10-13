package com.wanxia.gateway.core.filter.flow;

import com.wanxia.gateway.core.context.GatewayContext;

public interface RateLimiter {

    void tryConsume(GatewayContext context);

}
