package com.wanxia.gateway.cache.vector;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 向量查询接口
 */
public interface EmbeddingQuerier extends Provider {

    /**
     * 查询向量
     *
     * @param embedding 查询向量
     * @param callback 回调函数，接收查询结果或错误
     */
    void queryEmbedding(double[] embedding, BiConsumer<List<QueryResult>, Exception> callback);
}