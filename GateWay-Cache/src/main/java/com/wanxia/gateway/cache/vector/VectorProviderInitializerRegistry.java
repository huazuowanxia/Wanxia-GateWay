package com.wanxia.gateway.cache.vector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量数据库提供者初始化器注册表
 */
public class VectorProviderInitializerRegistry {

    private static final Map<String, VectorProviderInitializer> INITIALIZERS = new ConcurrentHashMap<>();

    static {
        INITIALIZERS.put(VectorConstants.PROVIDER_TYPE_QDRANT, new QdrantProviderInitializer());
    }

    /**
     * 获取提供者初始化器
     */
    public static VectorProviderInitializer getInitializer(String type) {
        return INITIALIZERS.get(type);
    }
}