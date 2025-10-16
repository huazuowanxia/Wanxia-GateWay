package com.wanxia.gateway.cache.vector;

/**
 * 向量数据库相关常量
 */
public class VectorConstants {

    // 向量数据库提供者类型
    public static final String PROVIDER_TYPE_DASH_VECTOR = "dashvector";
    public static final String PROVIDER_TYPE_CHROMA = "chroma";
    public static final String PROVIDER_TYPE_ES = "elasticsearch";
    public static final String PROVIDER_TYPE_WEAVIATE = "weaviate";
    public static final String PROVIDER_TYPE_PINECONE = "pinecone";
    public static final String PROVIDER_TYPE_QDRANT = "qdrant";
    public static final String PROVIDER_TYPE_MILVUS = "milvus";

    // 默认值
    public static final int DEFAULT_SERVICE_PORT = 443;
    public static final int DEFAULT_TOP_K = 1;
    public static final int DEFAULT_TIMEOUT = 10000;
    public static final double DEFAULT_THRESHOLD = 1000.0;
    public static final String DEFAULT_THRESHOLD_RELATION = "lt";

    // 阈值关系类型
    public static final String RELATION_LT = "lt";   // less than
    public static final String RELATION_LTE = "lte"; // less than or equal to
    public static final String RELATION_GT = "gt";   // greater than
    public static final String RELATION_GTE = "gte"; // greater than or equal to

    private VectorConstants() {
    }
}