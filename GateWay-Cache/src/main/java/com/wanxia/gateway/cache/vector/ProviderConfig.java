package com.wanxia.gateway.cache.vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.wanxia.gateway.cache.util.JsonUtil;
import lombok.Data;

/**
 * 向量数据库提供者配置
 */
@Data
public class ProviderConfig {

    /**
     * 向量存储服务提供者类型，例如 dashvector、chroma
     */
    private String type;

    /**
     * 向量存储服务名称
     */
    private String serviceName;

    /**
     * 向量存储服务域名
     */
    private String serviceHost;

    /**
     * 向量存储服务端口
     */
    private int servicePort;

    /**
     * 向量存储服务 API Key
     */
    private String apiKey;

    /**
     * 返回 TopK 结果，默认为 1
     */
    private int topK;

    /**
     * 请求超时时间，单位为毫秒。默认值是 10000，即 10 秒
     */
    private int timeout;

    /**
     * 向量存储服务的 Collection ID
     */
    private String collectionId;

    /**
     * 相似度度量阈值，默认为 1000
     */
    private double threshold;

    /**
     * 相似度度量比较方式，默认为 lt（小于）
     * 可选值：lt, lte, gt, gte
     */
    private String thresholdRelation;

    /**
     * ES 用户名
     */
    private String esUsername;

    /**
     * ES 密码
     */
    private String esPassword;

    /**
     * 从 JSON 解析配置
     */
    public void fromJson(JsonNode json) {
        this.type = JsonUtil.getStringValue(json, "type", null);
        this.serviceName = JsonUtil.getStringValue(json, "serviceName", null);
        this.serviceHost = JsonUtil.getStringValue(json, "serviceHost", null);

        this.servicePort = JsonUtil.getIntValue(json, "servicePort", VectorConstants.DEFAULT_SERVICE_PORT);
        this.topK = JsonUtil.getIntValue(json, "topK", VectorConstants.DEFAULT_TOP_K);
        this.timeout = JsonUtil.getIntValue(json, "timeout", VectorConstants.DEFAULT_TIMEOUT);

        this.apiKey = JsonUtil.getStringValue(json, "apiKey", null);
        this.collectionId = JsonUtil.getStringValue(json, "collectionID", null);

        this.threshold = JsonUtil.getDoubleValue(json, "threshold", VectorConstants.DEFAULT_THRESHOLD);
        this.thresholdRelation = JsonUtil.getStringValue(json, "thresholdRelation",
                VectorConstants.DEFAULT_THRESHOLD_RELATION);

        // ES 配置
        this.esUsername = JsonUtil.getStringValue(json, "esUsername", null);
        this.esPassword = JsonUtil.getStringValue(json, "esPassword", null);
    }

    /**
     * 验证配置
     */
    public void validate() {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("向量数据库服务类型不能为空");
        }

        VectorProviderInitializer initializer = VectorProviderInitializerRegistry.getInitializer(type);
        if (initializer == null) {
            throw new IllegalArgumentException("未知的向量数据库服务类型: " + type);
        }

        if (!isRelationValid(thresholdRelation)) {
            throw new IllegalArgumentException("无效的阈值关系: " + thresholdRelation);
        }

        initializer.validateConfig(this);
    }

    /**
     * 验证阈值关系是否有效
     */
    private boolean isRelationValid(String relation) {
        return VectorConstants.RELATION_LT.equals(relation) ||
                VectorConstants.RELATION_LTE.equals(relation) ||
                VectorConstants.RELATION_GT.equals(relation) ||
                VectorConstants.RELATION_GTE.equals(relation);
    }
}