package com.wanxia.gateway.cache.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存提供者初始化器注册表
 */
public class ProviderInitializerRegistry {

    private static final Map<String, ProviderInitializer> INITIALIZERS = new ConcurrentHashMap<>();

    static {
        // 注册 Redis 提供者初始化器
        INITIALIZERS.put(CacheConstants.PROVIDER_TYPE_REDIS, new RedisProviderInitializer());
    }

    public static void registerInitializer(String type, ProviderInitializer initializer) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Provider type cannot be null or empty");
        }
        if (initializer == null) {
            throw new IllegalArgumentException("Initializer cannot be null");
        }
        INITIALIZERS.put(type, initializer);
    }

    public static ProviderInitializer getInitializer(String type) {
        return INITIALIZERS.get(type);
    }

    public static boolean hasInitializer(String type) {
        return INITIALIZERS.containsKey(type);
    }

    private ProviderInitializerRegistry() {
    }
}

