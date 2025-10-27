package com.wanxia.gateway.tokenratelimit.config;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.wanxia.gateway.tokenratelimit.util.ProxyIpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * AI Token 限流配置解析器
 * @author zyl
 */
@Slf4j
public class AiTokenRateLimitConfigParser {

    /**
     * IP 来源类型：远程地址
     */
    public static final String REMOTE_ADDR_SOURCE_TYPE = "remote-addr";

    /**
     * IP 来源类型：请求头
     */
    public static final String HEADER_SOURCE_TYPE = "header";

    /**
     * 默认拒绝状态码
     */
    public static final int DEFAULT_REJECTED_CODE = 429;

    /**
     * 默认拒绝消息
     */
    public static final String DEFAULT_REJECTED_MSG = "Too many requests";

    /**
     * 时间窗口常量
     */
    private static final long SECOND = 1;
    private static final long SECONDS_PER_MINUTE = 60 * SECOND;
    private static final long SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;
    private static final long SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;

    /**
     * 时间窗口映射
     */
    private static final Map<String, Long> TIME_WINDOWS = new LinkedHashMap<>();

    static {
        TIME_WINDOWS.put("token_per_second", SECOND);
        TIME_WINDOWS.put("token_per_minute", SECONDS_PER_MINUTE);
        TIME_WINDOWS.put("token_per_hour", SECONDS_PER_HOUR);
        TIME_WINDOWS.put("token_per_day", SECONDS_PER_DAY);
    }

    /**
     * 初始化 Redis 集群客户端
     *
     * @param json   配置 JSON 对象
     * @param config 限流配置对象
     * @throws IllegalArgumentException 如果配置无效
     */
    public static void initRedisClusterClient(JSONObject json, AiTokenRateLimitConfig config) {
        JSONObject redisConfig = json.getJSONObject("redis");
        if (redisConfig == null) {
            throw new IllegalArgumentException("missing redis in config");
        }

        String serviceName = redisConfig.getString("service_name");
        if (StringUtils.isBlank(serviceName)) {
            throw new IllegalArgumentException("redis service name must not be empty");
        }

        int servicePort = redisConfig.getIntValue("service_port");
        if (servicePort == 0) {
            if (serviceName.endsWith(".static")) {
                // 使用默认逻辑端口 80 用于静态服务
                servicePort = 80;
            } else {
                servicePort = 6379;
            }
        }

        String username = redisConfig.getString("username");
        String password = redisConfig.getString("password");
        int timeout = redisConfig.getIntValue("timeout");
        if (timeout == 0) {
            timeout = 1000;
        }

        // TODO: 根据实际的 Redis 客户端库来初始化
        // config.setRedisClient(new RedisClusterClient(...));

        int database = redisConfig.getIntValue("database");
        log.info("redis init successfully");
    }

    /**
     * 解析 AI Token 限流配置
     *
     * @param json   配置 JSON 对象
     * @param config 限流配置对象
     * @throws IllegalArgumentException 如果配置无效
     */
    public static void parseAiTokenRateLimitConfig(JSONObject json, AiTokenRateLimitConfig config) {
        String ruleName = json.getString("rule_name");
        if (StringUtils.isBlank(ruleName)) {
            throw new IllegalArgumentException("missing rule_name in config");
        }
        config.setRuleName(ruleName);

        // 初始化限流规则
        initLimitRule(json, config);

        // 设置拒绝状态码
        Integer rejectedCode = json.getInteger("rejected_code");
        config.setRejectedCode(rejectedCode != null ? rejectedCode : DEFAULT_REJECTED_CODE);

        // 设置拒绝消息
        String rejectedMsg = json.getString("rejected_msg");
        config.setRejectedMsg(StringUtils.isNotBlank(rejectedMsg) ? rejectedMsg : DEFAULT_REJECTED_MSG);
    }

    /**
     * 初始化限流规则
     *
     * @param json   配置 JSON 对象
     * @param config 限流配置对象
     * @throws IllegalArgumentException 如果配置无效
     */
    private static void initLimitRule(JSONObject json, AiTokenRateLimitConfig config) {
        JSONObject globalThresholdResult = json.getJSONObject("global_threshold");
        Object ruleItemsResult = json.get("rule_items");

        boolean hasGlobal = globalThresholdResult != null;
        boolean hasRule = ruleItemsResult != null;

        if (!hasGlobal && !hasRule) {
            throw new IllegalArgumentException("at least one of 'global_threshold' or 'rule_items' must be set");
        } else if (hasGlobal && hasRule) {
            throw new IllegalArgumentException("'global_threshold' and 'rule_items' cannot be set at the same time");
        }

        // 处理全局限流配置
        if (hasGlobal) {
            GlobalThreshold threshold = parseGlobalThreshold(globalThresholdResult);
            config.setGlobalThreshold(threshold);
            return;
        }

        // 处理条件限流规则
        List<JSONObject> items = json.getJSONArray("rule_items").toList(JSONObject.class);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("config rule_items cannot be empty");
        }

        List<LimitRuleItem> ruleItems = new ArrayList<>();
        Set<String> seenLimitRules = new HashSet<>();

        for (JSONObject item : items) {
            LimitRuleItem ruleItem = parseLimitRuleItem(item);

            // 构造 LimitType 和 Key 的唯一标识
            String ruleKey = ruleItem.getLimitType().getValue() + ":" + ruleItem.getKey();

            // 检查是否有重复的 LimitType 和 Key 组合
            if (seenLimitRules.contains(ruleKey)) {
                log.warn("duplicate rule found: {}='{}' in rule_items", ruleItem.getLimitType(), ruleItem.getKey());
            } else {
                seenLimitRules.add(ruleKey);
            }

            ruleItems.add(ruleItem);
        }
        config.setRuleItems(ruleItems);
    }

    /**
     * 解析全局阈值配置
     *
     * @param item 配置 JSON 对象
     * @return 全局阈值对象
     * @throws IllegalArgumentException 如果配置无效
     */
    private static GlobalThreshold parseGlobalThreshold(JSONObject item) {
        for (Map.Entry<String, Long> entry : TIME_WINDOWS.entrySet()) {
            String timeWindowKey = entry.getKey();
            Long duration = entry.getValue();

            Long count = item.getLong(timeWindowKey);
            if (count != null && count > 0) {
                return new GlobalThreshold(count, duration);
            }
        }
        throw new IllegalArgumentException("one of 'token_per_second', 'token_per_minute', 'token_per_hour', or 'token_per_day' must be set for global_threshold");
    }

    /**
     * 解析限流规则项
     *
     * @param item 配置 JSON 对象
     * @return 限流规则项对象
     * @throws IllegalArgumentException 如果配置无效
     */
    private static LimitRuleItem parseLimitRuleItem(JSONObject item) {
        LimitRuleItem ruleItem = new LimitRuleItem();
        LimitRuleItemType limitType = null;

        // 根据配置区分限流类型
        if (StringUtils.isNotBlank(item.getString("limit_by_header"))) {
            ruleItem.setKey(item.getString("limit_by_header"));
            limitType = LimitRuleItemType.LIMIT_BY_HEADER;
        } else if (StringUtils.isNotBlank(item.getString("limit_by_param"))) {
            ruleItem.setKey(item.getString("limit_by_param"));
            limitType = LimitRuleItemType.LIMIT_BY_PARAM;
        } else if (StringUtils.isNotBlank(item.getString("limit_by_cookie"))) {
            ruleItem.setKey(item.getString("limit_by_cookie"));
            limitType = LimitRuleItemType.LIMIT_BY_COOKIE;
        } else if (StringUtils.isNotBlank(item.getString("limit_by_per_header"))) {
            ruleItem.setKey(item.getString("limit_by_per_header"));
            limitType = LimitRuleItemType.LIMIT_BY_PER_HEADER;
        } else if (StringUtils.isNotBlank(item.getString("limit_by_per_param"))) {
            ruleItem.setKey(item.getString("limit_by_per_param"));
            limitType = LimitRuleItemType.LIMIT_BY_PER_PARAM;
        } else if (StringUtils.isNotBlank(item.getString("limit_by_per_cookie"))) {
            ruleItem.setKey(item.getString("limit_by_per_cookie"));
            limitType = LimitRuleItemType.LIMIT_BY_PER_COOKIE;
        } else if (item.containsKey("limit_by_consumer")) {
            ruleItem.setKey(ProxyIpUtil.CONSUMER_HEADER);
            limitType = LimitRuleItemType.LIMIT_BY_CONSUMER;
        } else if (item.containsKey("limit_by_per_consumer")) {
            ruleItem.setKey(ProxyIpUtil.CONSUMER_HEADER);
            limitType = LimitRuleItemType.LIMIT_BY_PER_CONSUMER;
        } else if (StringUtils.isNotBlank(item.getString("limit_by_per_ip"))) {
            String limitByPerIp = item.getString("limit_by_per_ip");
            ruleItem.setKey(limitByPerIp);

            if (limitByPerIp.startsWith("from-header-")) {
                String headerName = limitByPerIp.substring("from-header-".length());
                if (StringUtils.isBlank(headerName)) {
                    throw new IllegalArgumentException("limit_by_per_ip parse error: empty after 'from-header-'");
                }
                ruleItem.setLimitByPerIp(new LimitByPerIp(HEADER_SOURCE_TYPE, headerName));
            } else if ("from-remote-addr".equals(limitByPerIp)) {
                ruleItem.setLimitByPerIp(new LimitByPerIp(REMOTE_ADDR_SOURCE_TYPE, ""));
            } else {
                throw new IllegalArgumentException("the 'limit_by_per_ip' restriction must start with 'from-header-' or be exactly 'from-remote-addr'");
            }
            limitType = LimitRuleItemType.LIMIT_BY_PER_IP;
        }

        if (limitType == null) {
            throw new IllegalArgumentException("only one of 'limit_by_header' and 'limit_by_param' and 'limit_by_consumer' and 'limit_by_cookie' and 'limit_by_per_header' and 'limit_by_per_param' and 'limit_by_per_consumer' and 'limit_by_per_cookie' and 'limit_by_per_ip' can be set");
        }
        ruleItem.setLimitType(limitType);

        // 初始化 configItems
        initConfigItems(item, ruleItem);

        return ruleItem;
    }

    /**
     * 初始化配置项
     *
     * @param json 配置 JSON 对象
     * @param rule 限流规则项
     * @throws IllegalArgumentException 如果配置无效
     */
    private static void initConfigItems(JSONObject json, LimitRuleItem rule) {
        Object limitKeysObj = json.get("limit_keys");
        if (limitKeysObj == null) {
            throw new IllegalArgumentException("missing limit_keys in config");
        }

        List<JSONObject> limitKeys = json.getJSONArray("limit_keys").toList(JSONObject.class);
        if (limitKeys.isEmpty()) {
            throw new IllegalArgumentException("config limit_keys cannot be empty");
        }

        List<LimitConfigItem> configItems = new ArrayList<>();
        for (JSONObject item : limitKeys) {
            String key = item.getString("key");
            if (StringUtils.isBlank(key)) {
                throw new IllegalArgumentException("limit_keys key is required");
            }

            LimitConfigItem configItem = createConfigItemFromRate(item, rule.getLimitType(), key);
            if (configItem != null) {
                configItems.add(configItem);
            }
        }
        rule.setConfigItems(configItems);
    }

    /**
     * 从速率配置创建配置项
     *
     * @param item      配置 JSON 对象
     * @param limitType 限流类型
     * @param key       限流 key
     * @return 限流配置项
     * @throws IllegalArgumentException 如果配置无效
     */
    private static LimitConfigItem createConfigItemFromRate(JSONObject item, LimitRuleItemType limitType, String key) {
        for (Map.Entry<String, Long> entry : TIME_WINDOWS.entrySet()) {
            String timeWindowKey = entry.getKey();
            Long duration = entry.getValue();

            Long count = item.getLong(timeWindowKey);
            if (count != null && count > 0) {
                LimitConfigItem configItem = new LimitConfigItem();
                configItem.setKey(key);
                configItem.setCount(count);
                configItem.setTimeWindow(duration);

                // 根据限流类型设置配置项类型
                if (limitType == LimitRuleItemType.LIMIT_BY_PER_IP) {
                    configItem.setConfigType(LimitConfigItemType.IP_NET);
                    configItem.setIpNet(key);
                } else if (limitType == LimitRuleItemType.LIMIT_BY_PER_HEADER ||
                        limitType == LimitRuleItemType.LIMIT_BY_PER_PARAM ||
                        limitType == LimitRuleItemType.LIMIT_BY_PER_CONSUMER ||
                        limitType == LimitRuleItemType.LIMIT_BY_PER_COOKIE) {
                    if ("*".equals(key)) {
                        configItem.setConfigType(LimitConfigItemType.ALL);
                    } else if (key.startsWith("regexp:")) {
                        String regexpStr = key.substring("regexp:".length());
                        try {
                            Pattern pattern = Pattern.compile(regexpStr);
                            configItem.setConfigType(LimitConfigItemType.REGEXP);
                            configItem.setRegexp(pattern);
                        } catch (PatternSyntaxException e) {
                            throw new IllegalArgumentException(String.format("failed to compile regex for key '%s': %s", key, e.getMessage()));
                        }
                    } else {
                        throw new IllegalArgumentException(String.format("the '%s' restriction must start with 'regexp:' or be exactly '*'", limitType.getValue()));
                    }
                } else {
                    configItem.setConfigType(LimitConfigItemType.EXACT);
                }

                return configItem;
            }
        }
        throw new IllegalArgumentException("one of 'token_per_second', 'token_per_minute', 'token_per_hour', or 'token_per_day' must be set for key: " + key);
    }
}

