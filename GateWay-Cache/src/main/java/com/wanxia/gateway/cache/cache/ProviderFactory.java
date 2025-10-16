package com.wanxia.gateway.cache.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 缓存提供者工厂
 */
public class ProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(ProviderFactory.class);

    public static Provider createProvider(ProviderConfig config) {
        return createProvider(config, log);
    }

    public static Provider createProvider(ProviderConfig config, Logger logger) {
        if (config == null) {
            throw new IllegalArgumentException("Provider config cannot be null");
        }

        String type = config.getType();
        ProviderInitializer initializer = ProviderInitializerRegistry.getInitializer(type);

        if (initializer == null) {
            throw new IllegalArgumentException("unknown provider type: " + type);
        }

        return initializer.createProvider(config, logger);
    }

    private ProviderFactory() {
    }
}

