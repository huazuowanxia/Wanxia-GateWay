package com.wanxia.gateway.core.filter.route;

import com.wanxia.gateway.common.enums.ResponseCode;
import com.wanxia.gateway.config.pojo.RouteDefinition;
import com.wanxia.gateway.core.context.GatewayContext;
import com.wanxia.gateway.core.filter.Filter;
import com.wanxia.gateway.core.helper.ContextHelper;
import com.wanxia.gateway.core.helper.ResponseHelper;
import com.wanxia.gateway.core.resilience.Resilience;
import org.asynchttpclient.Response;

import java.util.concurrent.CompletableFuture;

import static com.wanxia.gateway.common.constant.FilterConstant.ROUTE_FILTER_NAME;
import static com.wanxia.gateway.common.constant.FilterConstant.ROUTE_FILTER_ORDER;

public class RouteFilter implements Filter {

    @Override
    public void doPreFilter(GatewayContext context) {
        RouteDefinition.ResilienceConfig resilience = context.getRoute().getResilience();
        if (resilience.isEnabled()) { // 开启弹性配置
            Resilience.getInstance().executeRequest(context);
        } else {
            CompletableFuture<Response> future = RouteUtil.buildRouteSupplier(context).get().toCompletableFuture();
            future.exceptionally(throwable -> {
                context.setResponse(ResponseHelper.buildGatewayResponse(ResponseCode.HTTP_RESPONSE_ERROR));
                ContextHelper.writeBackResponse(context);
                return null;
            });
        }
    }

    @Override
    public void doPostFilter(GatewayContext context) {
        context.doFilter();
    }

    @Override
    public String mark() {
        return ROUTE_FILTER_NAME;
    }

    @Override
    public int getOrder() {
        return ROUTE_FILTER_ORDER;
    }

}
