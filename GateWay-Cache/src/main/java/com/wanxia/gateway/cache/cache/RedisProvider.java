package com.wanxia.gateway.cache.cache;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisException;

import java.util.function.Consumer;

/**
 * Redis 缓存提供者实现
 */
@Slf4j
public class RedisProvider implements Provider {

    private final ProviderConfig config;
    private static JedisPooled jedis;
    private volatile boolean ready = false;

    public RedisProvider(ProviderConfig config) {
        this.config = config;
    }

    @Override
    public String getProviderType() {
        return CacheConstants.PROVIDER_TYPE_REDIS;
    }

    @Override
    public void init(String username, String password, int timeout) {
        try {
            String host = config.getServiceHost() != null && !config.getServiceHost().isEmpty()
                    ? config.getServiceHost()
                    : config.getServiceName();

            int port = config.getServicePort();
            int database = config.getDatabase();

            // 创建 Jedis 连接池
            if (username != null && !username.isEmpty()) {
                jedis = new JedisPooled(host, port, username, password);
            } else if (password != null && !password.isEmpty()) {
                jedis = new JedisPooled(host, port, null, password);
            } else {
                jedis = new JedisPooled(host, port);
            }

            // 测试连接
            jedis.ping();
            ready = true;
            log.info("redis初始化成功, host: {}, port: {}, database: {}", host, port, database);
        } catch (Exception e) {
            ready = false;
            log.error("redis初始化失败, 稍后重试. Error: {}", e.getMessage(), e);
        }
    }

    @Override
    public String get(String key){
        return get(key, null);
    }


    @Override
    public String get(String key, Consumer<String> callback) {
        if (!ready) {
            throw new RuntimeException("redis未初始化");
        }
        String value = jedis.get(key);
        if (callback != null) {
            callback.accept(value);
        }
        return value;
    }

    @Override
    public void set(String key, String value) {
        set(key, value, null);
    }

    @Override
    public void set(String key, String value, Consumer<Boolean> callback) {
        if (!ready) {
            throw new RuntimeException("redis未初始化");
        }

        String result;
        if (config.getCacheTTL() == 0) {
            // 永不过期
            result = jedis.set(key, value);
        } else {
            // 设置过期时间（秒）
            result = jedis.setex(key, config.getCacheTTL(), value);
        }

        if (callback != null) {
            callback.accept("OK".equalsIgnoreCase(result));
        }
    }

    @Override
    public String getCacheKeyPrefix() {
        return config.getCacheKeyPrefix();
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (jedis != null) {
            try {
                jedis.close();
                log.info("Redis connection closed");
            } catch (Exception e) {
                log.error("Error closing Redis connection: {}", e.getMessage());
            }
        }
    }
}

