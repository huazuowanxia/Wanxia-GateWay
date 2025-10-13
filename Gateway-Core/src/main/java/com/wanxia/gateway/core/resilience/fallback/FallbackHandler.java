package com.wanxia.gateway.core.resilience.fallback;

import com.wanxia.gateway.core.context.GatewayContext;

public interface FallbackHandler {

    void handle(Throwable throwable, GatewayContext context);

    String mark();

}
