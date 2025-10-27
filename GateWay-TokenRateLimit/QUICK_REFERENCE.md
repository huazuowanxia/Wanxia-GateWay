# AI Token 限流配置 - 快速参考指南

## 📚 类结构概览

```
com.wanxia.gateway.tokenratelimit.config
├── LimitRuleItemType (enum)           # 限流规则类型
├── LimitConfigItemType (enum)         # 配置项类型
├── GlobalThreshold (POJO)             # 全局阈值
├── LimitByPerIp (POJO)                # IP 限流配置
├── LimitConfigItem (POJO)             # 配置项
├── LimitRuleItem (POJO)               # 规则项
├── AiTokenRateLimitConfig (POJO)      # 主配置
└── AiTokenRateLimitConfigParser       # 配置解析器
```

## 🎯 限流规则类型 (LimitRuleItemType)

| 类型 | 值 | 说明 |
|------|-----|------|
| LIMIT_BY_HEADER | limit_by_header | 按请求头限流 |
| LIMIT_BY_PARAM | limit_by_param | 按请求参数限流 |
| LIMIT_BY_CONSUMER | limit_by_consumer | 按消费者限流 |
| LIMIT_BY_COOKIE | limit_by_cookie | 按 Cookie 限流 |
| LIMIT_BY_PER_HEADER | limit_by_per_header | 按请求头值限流 |
| LIMIT_BY_PER_PARAM | limit_by_per_param | 按请求参数值限流 |
| LIMIT_BY_PER_CONSUMER | limit_by_per_consumer | 按消费者限流 |
| LIMIT_BY_PER_COOKIE | limit_by_per_cookie | 按 Cookie 值限流 |
| LIMIT_BY_PER_IP | limit_by_per_ip | 按 IP 限流 |

## 🔍 配置项类型 (LimitConfigItemType)

| 类型 | 值 | 说明 |
|------|-----|------|
| EXACT | exact | 精确匹配 |
| REGEXP | regexp | 正则表达式匹配 |
| ALL | * | 匹配所有 |
| IP_NET | ipNet | IP 段匹配 |

## ⏱️ 时间窗口常量

| 常量 | 值 | 说明 |
|------|-----|------|
| token_per_second | 1 | 每秒 |
| token_per_minute | 60 | 每分钟 |
| token_per_hour | 3600 | 每小时 |
| token_per_day | 86400 | 每天 |

## 📋 配置示例

### 全局限流配置

```json
{
  "rule_name": "global_limit",
  "global_threshold": {
    "token_per_minute": 1000
  },
  "rejected_code": 429,
  "rejected_msg": "Too many requests"
}
```

### 条件限流配置

```json
{
  "rule_name": "conditional_limit",
  "rule_items": [
    {
      "limit_by_header": "X-User-ID",
      "limit_keys": [
        {
          "key": "user123",
          "token_per_minute": 100
        },
        {
          "key": "*",
          "token_per_minute": 50
        }
      ]
    },
    {
      "limit_by_per_ip": "from-remote-addr",
      "limit_keys": [
        {
          "key": "192.168.1.0/24",
          "token_per_minute": 200
        }
      ]
    }
  ]
}
```

## 🔧 API 使用

### 解析配置

```java
import com.alibaba.fastjson2.JSONObject;
import com.wanxia.gateway.tokenratelimit.config.*;

// 1. 解析 JSON 配置
JSONObject configJson = JSONObject.parseObject(configString);

// 2. 创建配置对象
AiTokenRateLimitConfig config = new AiTokenRateLimitConfig();

// 3. 解析配置
AiTokenRateLimitConfigParser.parseAiTokenRateLimitConfig(configJson, config);

// 4. 初始化 Redis（可选）
AiTokenRateLimitConfigParser.initRedisClusterClient(configJson, config);
```

### 访问配置

```java
// 获取规则名称
String ruleName = config.getRuleName();

// 获取拒绝状态码
int rejectedCode = config.getRejectedCode();

// 获取拒绝消息
String rejectedMsg = config.getRejectedMsg();

// 获取全局阈值
GlobalThreshold globalThreshold = config.getGlobalThreshold();
if (globalThreshold != null) {
    long count = globalThreshold.getCount();
    long timeWindow = globalThreshold.getTimeWindow();
}

// 获取规则项
List<LimitRuleItem> ruleItems = config.getRuleItems();
for (LimitRuleItem item : ruleItems) {
    LimitRuleItemType limitType = item.getLimitType();
    String key = item.getKey();
    List<LimitConfigItem> configItems = item.getConfigItems();

    for (LimitConfigItem configItem : configItems) {
        LimitConfigItemType configType = configItem.getConfigType();
        long count = configItem.getCount();
        long timeWindow = configItem.getTimeWindow();
    }
}
```

## 🚀 常见场景

### 场景 1: 按用户 ID 限流

```json
{
  "rule_name": "user_limit",
  "rule_items": [
    {
      "limit_by_header": "X-User-ID",
      "limit_keys": [
        {
          "key": "user123",
          "token_per_minute": 100
        }
      ]
    }
  ]
}
```

### 场景 2: 按 IP 段限流

```json
{
  "rule_name": "ip_limit",
  "rule_items": [
    {
      "limit_by_per_ip": "from-remote-addr",
      "limit_keys": [
        {
          "key": "192.168.1.0/24",
          "token_per_minute": 500
        }
      ]
    }
  ]
}
```

### 场景 3: 按正则表达式限流

```json
{
  "rule_name": "regex_limit",
  "rule_items": [
    {
      "limit_by_per_header": "X-API-Key",
      "limit_keys": [
        {
          "key": "regexp:^key_.*",
          "token_per_minute": 200
        }
      ]
    }
  ]
}
```

### 场景 4: 按消费者限流

```json
{
  "rule_name": "consumer_limit",
  "rule_items": [
    {
      "limit_by_per_consumer": true,
      "limit_keys": [
        {
          "key": "*",
          "token_per_minute": 100
        }
      ]
    }
  ]
}
```

## ⚠️ 错误处理

所有解析方法都会抛出 `IllegalArgumentException`，包含详细的错误信息：

```java
try {
    AiTokenRateLimitConfigParser.parseAiTokenRateLimitConfig(configJson, config);
} catch (IllegalArgumentException e) {
    System.err.println("配置解析失败: " + e.getMessage());
}
```

常见错误：
- `missing rule_name in config` - 缺少规则名称
- `at least one of 'global_threshold' or 'rule_items' must be set` - 必须设置全局阈值或规则项
- `'global_threshold' and 'rule_items' cannot be set at the same time` - 不能同时设置两者
- `missing limit_keys in config` - 缺少限流 key 配置
- `one of 'token_per_second', 'token_per_minute', 'token_per_hour', or 'token_per_day' must be set` - 必须指定时间窗口

## 📖 相关文件

- `ProxyIpUtil.java` - IP 和请求头处理工具类
- `ProxyWasmUtil.java` - Proxy-Wasm 工具类（在 Gateway-Core 中）

