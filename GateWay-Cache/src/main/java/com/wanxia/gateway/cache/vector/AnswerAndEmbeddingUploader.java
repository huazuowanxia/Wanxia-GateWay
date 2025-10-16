package com.wanxia.gateway.cache.vector;

import java.util.function.Consumer;

/**
 * 答案和向量上传接口
 */
public interface AnswerAndEmbeddingUploader extends Provider {

    /**
     * 上传答案和向量
     *
     * @param queryString 查询字符串
     * @param queryEmbedding 查询向量
     * @param answer LLM 生成的答案
     * @param callback 回调函数，接收错误信息（如果有）
     */
    void uploadAnswerAndEmbedding(String queryString, double[] queryEmbedding,
                                  String answer, Consumer<Exception> callback);
}