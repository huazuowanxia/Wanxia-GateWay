package com.wanxia.gateway.core.resilience.fallback;

import com.wanxia.gateway.common.enums.ResponseCode;
import com.wanxia.gateway.core.context.GatewayContext;
import com.wanxia.gateway.core.helper.ContextHelper;
import com.wanxia.gateway.core.helper.ResponseHelper;

import static com.wanxia.gateway.common.constant.FallbackConstant.DEFAULT_FALLBACK_HANDLER_NAME;

public class DefaultFallbackHandler implements FallbackHandler {

    @Override
    public void handle(Throwable throwable, GatewayContext context) {
        context.setThrowable(throwable);
        context.setResponse(ResponseHelper.buildGatewayResponse(ResponseCode.GATEWAY_FALLBACK));
        ContextHelper.writeBackResponse(context);
    }

    @Override
    public String mark() {
        return DEFAULT_FALLBACK_HANDLER_NAME;
    }

}
