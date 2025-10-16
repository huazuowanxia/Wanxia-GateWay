package com.wanxia.gateway.cache.vector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量数据库提供者初始化器注册表
 */
public class VectorProviderInitializerRegistry {

    private static final Map<String, VectorProviderInitializer> INITIALIZERS = new ConcurrentHashMap<>();

    static {
        // 注册各种向量数据库提供者初始化器
        // INITIALIZERS.put(VectorConstants.PROVIDER_TYPE_DASH_VECTOR, new DashVectorProviderInitializer());
        // INITIALIZERS.put(VectorConstants.PROVIDER_TYPE_CHROMA, new ChromaProviderInitializer());
        // INITIALIZERS.put(VectorConstants.PROVIDER_TYPE_ES, new EsProviderInitializer());
        // INITIALIZERS.put(VectorConstants.PROVIDER_TYPE_WEAVIATE, new WeaviateProviderInitializer());
        // INITIALIZERS.put(VectorConstants.PROVIDER_TYPE_PINECONE, new PineconeProviderInitializer());
        // INITIALIZERS.put(VectorConstants.PROVIDER_TYPE_QDRANT, new QdrantProviderInitializer());
        // INITIALIZERS.put(VectorConstants.PROVIDER_TYPE_MILVUS, new MilvusProviderInitializer());
    }

    /**
     * 注册提供者初始化器
     */
    public static void registerInitializer(String type, VectorProviderInitializer initializer) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Provider type cannot be null or empty");
        }
        if (initializer == null) {
            throw new IllegalArgumentException("Initializer cannot be null");
        }
        INITIALIZERS.put(type, initializer);
    }

    /**
     * 获取提供者初始化器
     */
    public static VectorProviderInitializer getInitializer(String type) {
        return INITIALIZERS.get(type);
    }

    /**
     * 检查是否存在指定类型的初始化器
     */
    public static boolean hasInitializer(String type) {
        return INITIALIZERS.containsKey(type);
    }

    private VectorProviderInitializerRegistry() {
    }
}