package com.wanxia.gateway.config.helper;

import com.wanxia.gateway.common.enums.ResponseCode;
import com.wanxia.gateway.common.exception.NotFoundException;
import com.wanxia.gateway.config.manager.DynamicConfigManager;
import com.wanxia.gateway.config.pojo.RouteDefinition;

import java.util.*;
import java.util.regex.Pattern;

public class RouteResolver {

    private static final DynamicConfigManager manager = DynamicConfigManager.getInstance();

    /**
     * 根据uri解析出对应的路由
     */
    public static RouteDefinition matchingRouteByUri(String uri) {
        Set<Map.Entry<String, RouteDefinition>> allUriEntry = manager.getAllUriEntry();

        List<RouteDefinition> matchedRoute = new ArrayList<>();

        for (Map.Entry<String, RouteDefinition> entry: allUriEntry) {
            String regex = entry.getKey().replace("**", ".*");
            if (Pattern.matches(regex, uri)) {
                matchedRoute.add(entry.getValue());
            }
        }
        if (matchedRoute.isEmpty()) {
            throw new NotFoundException(ResponseCode.PATH_NO_MATCHED);
        }

        matchedRoute.sort(Comparator.comparingInt(RouteDefinition::getOrder));

        return matchedRoute.stream()
                .min(Comparator.comparingInt(RouteDefinition::getOrder)
                        .thenComparing(route -> route.getUri().length(), Comparator.reverseOrder()))
                .orElseThrow();
    }

}
