package com.wanxia.gateway.cache.vector;

/**
 * 向量数据库提供者初始化器接口
 */
public interface VectorProviderInitializer {

    /**
     * 验证配置
     *
     * @param config 提供者配置
     * @throws IllegalArgumentException 如果配置无效
     */
    void validateConfig(ProviderConfig config);

    /**
     * 创建提供者实例
     *
     * @param config 提供者配置
     * @return 提供者实例
     */
    Provider createProvider(ProviderConfig config);
}