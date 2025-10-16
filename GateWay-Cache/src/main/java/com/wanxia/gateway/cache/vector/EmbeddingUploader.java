package com.wanxia.gateway.cache.vector;

import java.util.function.Consumer;

/**
 * 向量上传接口
 */
public interface EmbeddingUploader extends Provider {

    /**
     * 上传向量
     *
     * @param queryString 查询字符串
     * @param queryEmbedding 查询向量
     * @param callback 回调函数，接收错误信息（如果有）
     */
    void uploadEmbedding(String queryString, double[] queryEmbedding, Consumer<Exception> callback);
}