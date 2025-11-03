package com.wanxia.gateway.cache;

import com.wanxia.gateway.cache.cache.Provider;
import com.wanxia.gateway.cache.vector.QueryResult;
import com.wanxia.gateway.cache.vector.StringQuerier;
import com.wanxia.gateway.cache.vector.EmbeddingQuerier;
import com.wanxia.gateway.core.request.GatewayRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * AI 缓存处理器
 * 处理缓存查询、相似度搜索、缓存命中等逻辑
 */
@Slf4j
public class AiCacheHandler {

    private static final String PLUGIN_NAME = "ai-cache";
    private static final String CACHE_KEY_CONTEXT_KEY = "cache_key";
    private static final String CACHE_KEY_EMBEDDING_KEY = "cache_key_embedding";

    /**
     * 检查缓存中是否存在指定的 key，如果不存在则触发相似度搜索
     *
     * @param key                  缓存 key
     * @param request              网关请求对象
     * @param cacheProvider        缓存提供者
     * @param vectorProvider       向量提供者
     * @param embeddingProvider    嵌入提供者
     * @param stream               是否为流式响应
     * @param useSimilaritySearch  是否使用相似度搜索
     * @param threshold            相似度阈值
     * @param thresholdRelation    阈值关系操作符
     * @return 是否成功处理
     */
    public static boolean checkCacheForKey(String key, GatewayRequest request, Provider cacheProvider,
                                           Object vectorProvider, Object embeddingProvider,
                                           boolean stream, boolean useSimilaritySearch,
                                           double threshold, String thresholdRelation) {
        if (cacheProvider == null) {
            log.debug("[{}] [checkCacheForKey] no cache provider configured, performing similarity search", PLUGIN_NAME);
            return performSimilaritySearch(key, request, cacheProvider, vectorProvider, embeddingProvider,
                    key, stream, threshold, thresholdRelation);
        }

        String queryKey = cacheProvider.getCacheKeyPrefix() + key;
        log.debug("[{}] [checkCacheForKey] querying cache with key: {}", PLUGIN_NAME, queryKey);

        String cachedValue = cacheProvider.get(queryKey);
        handleCacheResponse(key, cachedValue, request, stream, cacheProvider, vectorProvider,
                embeddingProvider, useSimilaritySearch, threshold, thresholdRelation);

        return true;
    }

    /**
     * 处理缓存响应，处理缓存命中和缓存未命中的情况
     */
    private static void handleCacheResponse(String key, String response, GatewayRequest request,
                                           boolean stream, Provider cacheProvider, Object vectorProvider,
                                           Object embeddingProvider, boolean useSimilaritySearch,
                                           double threshold, String thresholdRelation) {
        if (response != null && !response.isEmpty()) {
            log.info("[{}] cache hit for key: {}", PLUGIN_NAME, key);
            processCacheHit(key, response, stream, request, cacheProvider);
            return;
        }

        log.info("[{}] [handleCacheResponse] cache miss for key: {}", PLUGIN_NAME, key);

        if (useSimilaritySearch) {
            if (!performSimilaritySearch(key, request, cacheProvider, vectorProvider, embeddingProvider,
                    key, stream, threshold, thresholdRelation)) {
                log.error("[{}] [handleCacheResponse] failed to perform similarity search for key: {}", PLUGIN_NAME, key);
                // 继续处理请求
            }
        }
    }

    /**
     * 处理缓存命中
     */
    private static void processCacheHit(String key, String response, boolean stream,
                                       GatewayRequest request, Provider cacheProvider) {
        if (response == null || response.trim().isEmpty()) {
            log.warn("[{}] [processCacheHit] cached response for key {} is empty", PLUGIN_NAME, key);
            return;
        }

        log.debug("[{}] [processCacheHit] cached response for key {}: {}", PLUGIN_NAME, key, response);

        // 设置缓存状态
        request.setHitCache(true);

        if (stream) {
            // 流式响应处理
            String streamResponse = String.format("data: {\"content\": \"%s\"}\n\n", escapeJson(response));
            // 发送响应
        } else {
            // 非流式响应处理
            String jsonResponse = String.format("{\"content\": \"%s\"}", escapeJson(response));
            // 发送响应
        }
    }

    /**
     * 执行相似度搜索，确定使用哪种相似度搜索方法
     */
    private static boolean performSimilaritySearch(String key, GatewayRequest request, Provider cacheProvider,
                                                   Object vectorProvider, Object embeddingProvider,
                                                   String queryString, boolean stream,
                                                   double threshold, String thresholdRelation) {
        if (vectorProvider == null) {
            log.error("[{}] [performSimilaritySearch] no vector provider configured for similarity search", PLUGIN_NAME);
            return false;
        }

        // 检查是否实现了 StringQuerier 接口
        if (vectorProvider instanceof StringQuerier) {
            log.debug("[{}] [performSimilaritySearch] active vector provider implements StringQuerier interface", PLUGIN_NAME);
            return performStringQuery(key, queryString, request, cacheProvider, vectorProvider,
                    stream, threshold, thresholdRelation);
        }

        // 检查是否实现了 EmbeddingQuerier 接口
        if (vectorProvider instanceof EmbeddingQuerier) {
            log.debug("[{}] [performSimilaritySearch] active vector provider implements EmbeddingQuerier interface", PLUGIN_NAME);
            return performEmbeddingQuery(key, request, cacheProvider, vectorProvider, embeddingProvider,
                    stream, threshold, thresholdRelation);
        }

        log.error("[{}] [performSimilaritySearch] no suitable querier or embedding provider available", PLUGIN_NAME);
        return false;
    }

    /**
     * 执行基于字符串的相似度搜索
     */
    private static boolean performStringQuery(String key, String queryString, GatewayRequest request,
                                             Provider cacheProvider, Object vectorProvider,
                                             boolean stream, double threshold, String thresholdRelation) {
        if (!(vectorProvider instanceof StringQuerier)) {
            log.error("[{}] [performStringQuery] active vector provider does not implement StringQuerier interface", PLUGIN_NAME);
            return false;
        }

        StringQuerier stringQuerier = (StringQuerier) vectorProvider;
        stringQuerier.queryString(queryString, (results, err) -> {
            handleQueryResults(key, results, request, stream, cacheProvider, vectorProvider, err, threshold, thresholdRelation);
        });

        return true;
    }

    /**
     * 执行基于嵌入的相似度搜索
     */
    private static boolean performEmbeddingQuery(String key, GatewayRequest request, Provider cacheProvider,
                                                Object vectorProvider, Object embeddingProvider,
                                                boolean stream, double threshold, String thresholdRelation) {
        if (!(vectorProvider instanceof EmbeddingQuerier)) {
            log.error("[{}] [performEmbeddingQuery] active vector provider does not implement EmbeddingQuerier interface", PLUGIN_NAME);
            return false;
        }

        if (embeddingProvider == null) {
            log.error("[{}] [performEmbeddingQuery] no embedding provider configured for similarity search", PLUGIN_NAME);
            return false;
        }

        // 获取嵌入向量
        // embeddingProvider.getEmbedding(key, request, (embedding, err) -> {
        //     if (err != null) {
        //         handleInternalError(err, String.format("[%s] error getting embedding for key: %s", PLUGIN_NAME, key));
        //         return;
        //     }
        //
        //     request.setAttribute(CACHE_KEY_EMBEDDING_KEY, embedding);
        //
        //     EmbeddingQuerier embeddingQuerier = (EmbeddingQuerier) vectorProvider;
        //     embeddingQuerier.queryEmbedding(embedding, request, (results, ctx, err2) -> {
        //         handleQueryResults(key, results, ctx, stream, cacheProvider, vectorProvider, err2, threshold, thresholdRelation);
        //     });
        // });

        return true;
    }

    /**
     * 处理相似度搜索的结果
     */
    private static void handleQueryResults(String key, List<QueryResult> results, GatewayRequest request,
                                          boolean stream, Provider cacheProvider, Object vectorProvider,
                                          Exception err, double threshold, String thresholdRelation) {
        if (err != null) {
            handleInternalError(err, String.format("[%s] error querying vector database for key: %s", PLUGIN_NAME, key));
            return;
        }

        if (results == null || results.isEmpty()) {
            log.warn("[{}] [handleQueryResults] no similar keys found for key: {}", PLUGIN_NAME, key);
            return;
        }

        QueryResult mostSimilarData = results.get(0);
        log.debug("[{}] [handleQueryResults] for key: {}, the most similar key found: {} with score: {}",
                PLUGIN_NAME, key, mostSimilarData.getText(), mostSimilarData.getScore());

        if (compare(thresholdRelation, mostSimilarData.getScore(), threshold)) {
            log.info("[{}] key accepted: {} with score: {}", PLUGIN_NAME, mostSimilarData.getText(), mostSimilarData.getScore());

            if (mostSimilarData.getAnswer() != null && !mostSimilarData.getAnswer().isEmpty()) {
                // 直接返回答案
                cacheResponse(request, cacheProvider, key, mostSimilarData.getAnswer());
                processCacheHit(key, mostSimilarData.getAnswer(), stream, request, cacheProvider);
            } else {
                if (cacheProvider != null) {
                    checkCacheForKey(mostSimilarData.getText(), request, cacheProvider, vectorProvider, null,
                            stream, false, threshold, thresholdRelation);
                } else {
                    log.info("[{}] cache hit for key: {}, but no corresponding answer found in the vector database",
                            PLUGIN_NAME, mostSimilarData.getText());
                }
            }
        } else {
            log.info("[{}] score not meet the threshold {}: {} with score {}",
                    PLUGIN_NAME, threshold, mostSimilarData.getText(), mostSimilarData.getScore());
        }
    }

    /**
     * 缓存响应值
     */
    private static void cacheResponse(GatewayRequest request, Provider cacheProvider, String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            log.warn("[{}] [cacheResponse] cached value for key {} is empty", PLUGIN_NAME, key);
            return;
        }

        if (cacheProvider != null) {
            String queryKey = cacheProvider.getCacheKeyPrefix() + key;
            cacheProvider.set(queryKey, value);
            log.debug("[{}] [cacheResponse] cache set success, key: {}, length of value: {}", PLUGIN_NAME, queryKey, value.length());
        }
    }

    /**
     * 比较函数，用于相似度/距离/点积判断
     * 余弦相似度度量的是两个向量在方向上的相似程度。相似度越高，两个向量越接近。
     * 距离度量的是两个向量在空间上的远近程度。距离越小，两个向量越接近。
     *
     * @param operator 操作符 (gt, gte, lt, lte)
     * @param value1   第一个值
     * @param value2   第二个值
     * @return 比较结果
     */
    private static boolean compare(String operator, double value1, double value2) {
        switch (operator) {
            case "gt":
                return value1 > value2;
            case "gte":
                return value1 >= value2;
            case "lt":
                return value1 < value2;
            case "lte":
                return value1 <= value2;
            default:
                return false;
        }
    }

    /**
     * 处理内部错误
     */
    private static void handleInternalError(Exception err, String message) {
        if (err != null) {
            log.error("[{}] [handleInternalError] {}: {}", PLUGIN_NAME, message, err.getMessage());
        } else {
            log.error("[{}] [handleInternalError] {}", PLUGIN_NAME, message);
        }
    }

    /**
     * 转义 JSON 字符串
     */
    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}