package com.wanxia.gateway.cache.cache;

import org.slf4j.Logger;

/**
 * 缓存提供者初始化器接口
 */
public interface ProviderInitializer {

    void validateConfig(ProviderConfig config);

    Provider createProvider(ProviderConfig config, Logger logger);
}

