package com.wanxia.gateway.cache.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.wanxia.gateway.cache.util.JsonUtil;
import lombok.Data;

/**
 * 缓存提供者配置
 */
@Data
public class ProviderConfig {

    private String type;
    private String serviceName;
    private int servicePort;
    private String serviceHost;
    private String username;
    private String password;
    private int timeout;
    private int cacheTTL;
    private String cacheKeyPrefix;
    private int database;

    public void fromJson(JsonNode json) {
        this.type = JsonUtil.getStringValue(json, "type", null);
        this.serviceName = JsonUtil.getStringValue(json, "serviceName", null);

        if (json.has("servicePort")) {
            this.servicePort = json.get("servicePort").asInt();
        } else {
            if (serviceName != null && serviceName.endsWith(".static")) {
                this.servicePort = CacheConstants.DEFAULT_STATIC_SERVICE_PORT;
            } else {
                this.servicePort = CacheConstants.DEFAULT_REDIS_PORT;
            }
        }

        this.serviceHost = JsonUtil.getStringValue(json, "serviceHost", null);
        this.username = JsonUtil.getStringValue(json, "username", "");
        this.password = JsonUtil.getStringValue(json, "password", "");
        this.database = JsonUtil.getIntValue(json, "database", CacheConstants.DEFAULT_DATABASE);
        this.timeout = JsonUtil.getIntValue(json, "timeout", CacheConstants.DEFAULT_TIMEOUT);
        this.cacheTTL = JsonUtil.getIntValue(json, "cacheTTL", CacheConstants.DEFAULT_CACHE_TTL);
        this.cacheKeyPrefix = JsonUtil.getStringValue(json, "cacheKeyPrefix", CacheConstants.DEFAULT_CACHE_PREFIX);
    }

    public void validate() {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("缺少缓存类型");
        }
        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalArgumentException("缺少缓存服务名");
        }
        if (cacheTTL < 0) {
            throw new IllegalArgumentException("缓存超时时间必须大于等于0");
        }

        ProviderInitializer initializer = ProviderInitializerRegistry.getInitializer(type);
        if (initializer == null) {
            throw new IllegalArgumentException("未知的缓存服务: " + type);
        }
        initializer.validateConfig(this);
    }

}

