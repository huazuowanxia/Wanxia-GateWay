package com.wanxia.gateway.cache.vector;

/**
 * 向量数据库提供者工厂
 */
public class VectorProviderFactory {
    
    /**
     * 创建向量数据库提供者
     * 
     * @param config 提供者配置
     * @return 提供者实例
     * @throws IllegalArgumentException 如果提供者类型未知
     */
    public static Provider createProvider(ProviderConfig config) {
        if (config == null) throw new IllegalArgumentException("向量数据库配置不能为空");

        String type = config.getType();
        VectorProviderInitializer initializer = VectorProviderInitializerRegistry.getInitializer(type);
        
        if (initializer == null) throw new IllegalArgumentException("未知的类型: " + type);

        return initializer.createProvider(config);
    }

}