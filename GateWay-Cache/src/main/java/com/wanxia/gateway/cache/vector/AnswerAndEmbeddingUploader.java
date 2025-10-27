package com.wanxia.gateway.cache.vector;

import java.util.function.Consumer;

/**
 * 答案和向量上传接口
 */
public interface AnswerAndEmbeddingUploader extends Provider {

    Boolean uploadAnswerAndEmbedding(String queryString, double[] queryEmbedding, String answer);
}