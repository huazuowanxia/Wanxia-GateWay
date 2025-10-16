package com.wanxia.gateway.cache.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存使用示例
 *
 * 注意：运行此测试前，请确保本地 Redis 服务已启动
 * 启动命令：redis-server
 */
public class RedisCacheExample {

    private Provider provider;

    @Before
    public void setUp() {
        // 方式1：直接创建配置
        ProviderConfig config = new ProviderConfig();
        config.setType(CacheConstants.PROVIDER_TYPE_REDIS);
        config.setServiceName("localhost");
        config.setServicePort(6379);
        config.setServiceHost("127.0.0.1");
        config.setUsername("");
        config.setPassword("");
        config.setDatabase(0);
        config.setTimeout(10000);
        config.setCacheTTL(3600); // 1小时过期
        config.setCacheKeyPrefix("test-cache:");

        // 验证配置
        config.validate();

        // 创建提供者
        provider = ProviderFactory.createProvider(config);

        System.out.println("Redis Provider 初始化完成");
        System.out.println("提供者类型: " + provider.getProviderType());
        System.out.println("缓存前缀: " + provider.getCacheKeyPrefix());
    }

    /**
     * 示例1：基本的 set 和 get 操作
     */
    @Test
    public void testBasicSetAndGet() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        String key = provider.getCacheKeyPrefix() + "user:1001";
        String value = "{\"id\":1001,\"name\":\"张三\",\"age\":25}";

        System.out.println("\n=== 示例1：基本的 set 和 get 操作 ===");

        // 设置缓存
        provider.set(key, value, success -> {
            if (success) {
                System.out.println("✓ 缓存设置成功");
                System.out.println("  Key: " + key);
                System.out.println("  Value: " + value);
            } else {
                System.out.println("✗ 缓存设置失败");
            }
            latch.countDown();
        });

        // 等待一下，确保 set 完成
        Thread.sleep(100);

        // 获取缓存
        provider.get(key, result -> {
            if (result != null) {
                System.out.println("✓ 缓存获取成功");
                System.out.println("  获取到的值: " + result);
            } else {
                System.out.println("✗ 缓存未命中");
            }
            latch.countDown();
        });

        // 等待所有操作完成
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * 示例2：从 JSON 配置创建
     */
    @Test
    public void testCreateFromJson() throws Exception {
        System.out.println("\n=== 示例2：从 JSON 配置创建 ===");

        String jsonConfig = "{\n" +
                "  \"type\": \"redis\",\n" +
                "  \"serviceName\": \"localhost\",\n" +
                "  \"servicePort\": 6379,\n" +
                "  \"serviceHost\": \"127.0.0.1\",\n" +
                "  \"username\": \"\",\n" +
                "  \"password\": \"\",\n" +
                "  \"database\": 0,\n" +
                "  \"timeout\": 10000,\n" +
                "  \"cacheTTL\": 7200,\n" +
                "  \"cacheKeyPrefix\": \"json-cache:\"\n" +
                "}";

        System.out.println("JSON 配置:");
        System.out.println(jsonConfig);

        // 解析 JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(jsonConfig);

        // 创建配置
        ProviderConfig config = new ProviderConfig();
        config.fromJson(json);
        config.validate();

        // 创建提供者
        Provider jsonProvider = ProviderFactory.createProvider(config);

        System.out.println("\n从 JSON 创建的 Provider:");
        System.out.println("  类型: " + jsonProvider.getProviderType());
        System.out.println("  前缀: " + jsonProvider.getCacheKeyPrefix());

        // 测试使用
        CountDownLatch latch = new CountDownLatch(1);
        String key = jsonProvider.getCacheKeyPrefix() + "test";
        jsonProvider.set(key, "测试数据", success -> {
            System.out.println("  设置结果: " + (success ? "成功" : "失败"));
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * 示例3：旧版 JSON 配置兼容
     */
    @Test
    public void testLegacyJsonConfig() throws Exception {
        System.out.println("\n=== 示例3：旧版 JSON 配置兼容 ===");

        String legacyJson = "{\n" +
                "  \"redis\": {\n" +
                "    \"serviceName\": \"localhost\",\n" +
                "    \"servicePort\": 6379,\n" +
                "    \"serviceHost\": \"127.0.0.1\"\n" +
                "  },\n" +
                "  \"cacheTTL\": 1800\n" +
                "}";

        System.out.println("旧版 JSON 配置:");
        System.out.println(legacyJson);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(legacyJson);

        ProviderConfig config = new ProviderConfig();
        config.validate();

        System.out.println("\n解析后的配置:");
        System.out.println("  类型: " + config.getType());
        System.out.println("  服务名: " + config.getServiceName());
        System.out.println("  端口: " + config.getServicePort());
        System.out.println("  TTL: " + config.getCacheTTL() + " 秒");
    }

    /**
     * 示例4：批量操作
     */
    @Test
    public void testBatchOperations() throws InterruptedException {
        System.out.println("\n=== 示例4：批量操作 ===");

        int count = 5;
        CountDownLatch setLatch = new CountDownLatch(count);
        CountDownLatch getLatch = new CountDownLatch(count);

        // 批量设置
        System.out.println("批量设置缓存...");
        for (int i = 1; i <= count; i++) {
            String key = provider.getCacheKeyPrefix() + "product:" + i;
            String value = "{\"id\":" + i + ",\"name\":\"商品" + i + "\",\"price\":" + (i * 100) + "}";

            provider.set(key, value, success -> {
                System.out.println("  设置 " + key + ": " + (success ? "✓" : "✗"));
                setLatch.countDown();
            });
        }

        setLatch.await(5, TimeUnit.SECONDS);

        // 等待一下
        Thread.sleep(100);

        // 批量获取
        System.out.println("\n批量获取缓存...");
        for (int i = 1; i <= count; i++) {
            String key = provider.getCacheKeyPrefix() + "product:" + i;

            provider.get(key, value -> {
                if (value != null) {
                    System.out.println("  获取 " + key + ": " + value);
                }
                getLatch.countDown();
            });
        }

        getLatch.await(5, TimeUnit.SECONDS);
    }

    /**
     * 示例5：缓存未命中场景
     */
    @Test
    public void testCacheMiss() throws InterruptedException {
        System.out.println("\n=== 示例5：缓存未命中场景 ===");

        CountDownLatch latch = new CountDownLatch(1);
        String nonExistentKey = provider.getCacheKeyPrefix() + "non-existent-key";

        provider.get(nonExistentKey, value -> {
            if (value == null) {
                System.out.println("✓ 缓存未命中（符合预期）");
                System.out.println("  Key: " + nonExistentKey);
                System.out.println("  可以在这里执行数据库查询等回源操作");
            } else {
                System.out.println("✗ 意外获取到值: " + value);
            }
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * 示例6：不同 TTL 配置
     */
    @Test
    public void testDifferentTTL() throws InterruptedException {
        System.out.println("\n=== 示例6：不同 TTL 配置 ===");

        // 永不过期的配置
        ProviderConfig neverExpireConfig = new ProviderConfig();
        neverExpireConfig.setType(CacheConstants.PROVIDER_TYPE_REDIS);
        neverExpireConfig.setServiceName("localhost");
        neverExpireConfig.setServicePort(6379);
        neverExpireConfig.setServiceHost("127.0.0.1");
        neverExpireConfig.setCacheTTL(0); // 永不过期
        neverExpireConfig.setCacheKeyPrefix("never-expire:");
        neverExpireConfig.validate();

        Provider neverExpireProvider = ProviderFactory.createProvider(neverExpireConfig);

        CountDownLatch latch = new CountDownLatch(2);

        // 设置永不过期的缓存
        neverExpireProvider.set("never-expire:key1", "永久数据", success -> {
            System.out.println("永不过期缓存设置: " + (success ? "✓" : "✗"));
            System.out.println("  TTL: 0 (永不过期)");
            latch.countDown();
        });

        // 设置有过期时间的缓存（使用当前 provider，TTL=3600）
        provider.set(provider.getCacheKeyPrefix() + "key2", "临时数据", success -> {
            System.out.println("有过期时间缓存设置: " + (success ? "✓" : "✗"));
            System.out.println("  TTL: 3600 秒 (1小时)");
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);
    }
}

