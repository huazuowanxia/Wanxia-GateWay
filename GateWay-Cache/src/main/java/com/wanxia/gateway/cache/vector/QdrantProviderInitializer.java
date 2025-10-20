package com.wanxia.gateway.cache.vector;

/**
 * Qdrant 提供者初始化器
 */
public class QdrantProviderInitializer implements VectorProviderInitializer {

    @Override
    public void validateConfig(ProviderConfig config) {
        if (config.getServiceName() == null || config.getServiceName().isEmpty()) {
            throw new IllegalArgumentException("[Qdrant] serviceName is required");
        }
        if (config.getCollectionId() == null || config.getCollectionId().isEmpty()) {
            throw new IllegalArgumentException("[Qdrant] collectionID is required");
        }
    }

    @Override
    public Provider createProvider(ProviderConfig config) {
        return new QdrantProvider(config);
    }
}

