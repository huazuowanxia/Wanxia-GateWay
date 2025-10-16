package com.wanxia.gateway.cache.cache;

import com.fasterxml.jackson.databind.JsonNode;
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
        this.type = getStringValue(json, "type", null);
        this.serviceName = getStringValue(json, "serviceName", null);

        if (json.has("servicePort")) {
            this.servicePort = json.get("servicePort").asInt();
        } else {
            if (serviceName != null && serviceName.endsWith(".static")) {
                this.servicePort = CacheConstants.DEFAULT_STATIC_SERVICE_PORT;
            } else {
                this.servicePort = CacheConstants.DEFAULT_REDIS_PORT;
            }
        }

        this.serviceHost = getStringValue(json, "serviceHost", null);
        this.username = getStringValue(json, "username", "");
        this.password = getStringValue(json, "password", "");
        this.database = getIntValue(json, "database", CacheConstants.DEFAULT_DATABASE);
        this.timeout = getIntValue(json, "timeout", CacheConstants.DEFAULT_TIMEOUT);
        this.cacheTTL = getIntValue(json, "cacheTTL", CacheConstants.DEFAULT_CACHE_TTL);
        this.cacheKeyPrefix = getStringValue(json, "cacheKeyPrefix", CacheConstants.DEFAULT_CACHE_PREFIX);
    }

    public void convertLegacyJson(JsonNode json) {
        if (json.has("redis")) {
            fromJson(json.get("redis"));
        }
        this.type = CacheConstants.PROVIDER_TYPE_REDIS;

        if (json.has("cacheTTL")) {
            this.cacheTTL = json.get("cacheTTL").asInt();
        }
    }

    public void validate() {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("cache service type is required");
        }
        if (serviceName == null || serviceName.isEmpty()) {
            throw new IllegalArgumentException("cache service name is required");
        }
        if (cacheTTL < 0) {
            throw new IllegalArgumentException("cache TTL must be greater than or equal to 0");
        }

        ProviderInitializer initializer = ProviderInitializerRegistry.getInitializer(type);
        if (initializer == null) {
            throw new IllegalArgumentException("unknown cache service provider type: " + type);
        }
        initializer.validateConfig(this);
    }

    private String getStringValue(JsonNode json, String fieldName, String defaultValue) {
        if (json.has(fieldName) && !json.get(fieldName).isNull()) {
            return json.get(fieldName).asText();
        }
        return defaultValue;
    }

    private int getIntValue(JsonNode json, String fieldName, int defaultValue) {
        if (json.has(fieldName)) {
            return json.get(fieldName).asInt(defaultValue);
        }
        return defaultValue;
    }
}

