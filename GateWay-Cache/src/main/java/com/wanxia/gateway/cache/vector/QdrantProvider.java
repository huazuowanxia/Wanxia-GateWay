package com.wanxia.gateway.cache.vector;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.wanxia.gateway.cache.vector.VectorConstants.PUT_COLLECTIONS;

/**
 * Qdrant 向量数据库提供者实现
 */
@Slf4j
public class QdrantProvider implements Provider, EmbeddingQuerier, AnswerAndEmbeddingUploader {

    private final ProviderConfig config;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final String baseUrl;

    public QdrantProvider(ProviderConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();

        // 配置超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.getTimeout())
                .setSocketTimeout(config.getTimeout())
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // 构建基础 URL
        String host = config.getServiceHost() != null && !config.getServiceHost().isEmpty()
                ? config.getServiceHost()
                : config.getServiceName();
        this.baseUrl = String.format("http://%s:%d", host, config.getServicePort());
    }

    @Override
    public String getProviderType() {
        return VectorConstants.PROVIDER_TYPE_QDRANT;
    }

    @Override
    public Boolean uploadAnswerAndEmbedding(String queryString, double[] queryEmbedding, String answer) {
        try {
            // 构建请求体
            QdrantInsertRequest request = new QdrantInsertRequest();
            QdrantPoint point = new QdrantPoint();
            point.setId(UUID.randomUUID().toString());
            point.setVector(queryEmbedding);

            QdrantPayload payload = new QdrantPayload();
            payload.setQuestion(queryString);
            payload.setAnswer(answer);
            point.setPayload(payload);

            request.setPoints(List.of(point));

            // 发送 PUT 请求
            String url = String.format(PUT_COLLECTIONS, baseUrl, config.getCollectionId());
            String requestBody = objectMapper.writeValueAsString(request);

            log.debug("[Qdrant] Upload request: {}", requestBody);

            HttpPut httpPut = new HttpPut(url);
            httpPut.setHeader("Content-Type", "application/json");
            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                httpPut.setHeader("api-key", config.getApiKey());
            }
            httpPut.setEntity(new StringEntity(requestBody, "UTF-8"));

            CloseableHttpResponse response = httpClient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            log.debug("[Qdrant] Upload response: statusCode={}, body={}", statusCode, responseBody);
            if (statusCode < 200 && statusCode >= 300) {
                throw new RuntimeException(String.format("[Qdrant] Upload failed with status %d: %s", statusCode, responseBody));
            }

        } catch (Exception e) {
            log.error("[Qdrant] Failed to upload embedding: {}", e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public List<QueryResult> queryEmbedding(double[] embedding, BiConsumer<List<QueryResult>, Exception> callback) {
        List<QueryResult> results = new ArrayList<>();
        try {
            // 构建查询请求
            QdrantQueryRequest request = new QdrantQueryRequest();
            request.setVector(embedding);
            request.setLimit(config.getTopK());
            request.setWithPayload(true);

            // 发送 POST 请求
            String url = String.format("%s/collections/%s/points/search", baseUrl, config.getCollectionId());
            String requestBody = objectMapper.writeValueAsString(request);

            log.debug("[Qdrant] Query request: {}", requestBody);

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                httpPost.setHeader("api-key", config.getApiKey());
            }
            httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                log.debug("[Qdrant] Query response: statusCode={}, body={}", statusCode, responseBody);

                if (statusCode < 200 && statusCode >= 300) {
                    throw new RuntimeException(String.format("[Qdrant] Query failed with status %d: %s", statusCode, responseBody));
                }
                results = parseQueryResponse(responseBody);
            }

        } catch (Exception e) {
            log.error("[Qdrant] Failed to query embedding: {}", e.getMessage(), e);
        }

        return results;
    }

    /**
     * 解析查询响应
     */
    private List<QueryResult> parseQueryResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode resultArray = root.get("result");

        if (resultArray == null || !resultArray.isArray() || resultArray.size() == 0) {
            log.error("[Qdrant] No result found in response body: {}", responseBody);
            throw new RuntimeException("[Qdrant] No result found in response body");
        }

        List<QueryResult> results = new ArrayList<>();

        for (JsonNode resultNode : resultArray) {
            JsonNode scoreNode = resultNode.get("score");
            JsonNode payloadNode = resultNode.get("payload");

            if (scoreNode == null) {
                log.error("[Qdrant] No score found in response body: {}", responseBody);
                continue;
            }

            if (payloadNode == null || payloadNode.get("answer") == null) {
                log.error("[Qdrant] No answer found in response body: {}", responseBody);
                continue;
            }

            QueryResult result = new QueryResult();
            result.setScore(scoreNode.asDouble());
            result.setText(payloadNode.has("question") ? payloadNode.get("question").asText() : "");
            result.setAnswer(payloadNode.get("answer").asText());

            results.add(result);
        }

        return results;
    }

    /**
     * 关闭 HTTP 客户端
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            log.error("[Qdrant] Failed to close http client: {}", e.getMessage());
        }
    }

    // ========== 内部类：请求和响应对象 ==========

    /**
     * Qdrant Payload
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QdrantPayload {
        private String question;
        private String answer;
    }

    /**
     * Qdrant Point
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QdrantPoint {
        private String id;
        private double[] vector;
        private QdrantPayload payload;
    }

    /**
     * Qdrant 插入请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QdrantInsertRequest {
        private List<QdrantPoint> points;
    }

    /**
     * Qdrant 查询请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QdrantQueryRequest {
        private double[] vector;
        private int limit;
        @JsonProperty("with_payload")
        private boolean withPayload;
    }
}

