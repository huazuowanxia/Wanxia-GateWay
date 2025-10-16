package com.wanxia.gateway.cache.cache;

import java.util.function.Consumer;

/**
 * 缓存提供者接口
 */
public interface Provider {

    String getProviderType();

    void init(String username, String password, int timeout);

    void get(String key, Consumer<String> callback);

    void set(String key, String value, Consumer<Boolean> callback);

    String getCacheKeyPrefix();
}

