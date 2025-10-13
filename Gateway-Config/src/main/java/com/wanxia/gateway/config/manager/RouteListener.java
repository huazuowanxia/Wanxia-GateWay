package com.wanxia.gateway.config.manager;

import com.wanxia.gateway.config.pojo.RouteDefinition;

public interface RouteListener {

    void changeOnRoute(RouteDefinition routeDefinition);

}
