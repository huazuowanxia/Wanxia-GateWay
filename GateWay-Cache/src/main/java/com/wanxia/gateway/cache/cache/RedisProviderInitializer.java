package com.wanxia.gateway.cache.cache;

import org.slf4j.Logger;

/**
 * Redis 缓存提供者初始化器
 */
public class RedisProviderInitializer implements ProviderInitializer {

    @Override
    public void validateConfig(ProviderConfig config) {
        if (config.getServiceName() == null || config.getServiceName().isEmpty()) {
            throw new IllegalArgumentException("cache service name is required");
        }
    }

    @Override
    public Provider createProvider(ProviderConfig config, Logger logger) {
        RedisProvider provider = new RedisProvider(config, logger);
        provider.init(config.getUsername(), config.getPassword(), config.getTimeout());
        return provider;
    }
}

