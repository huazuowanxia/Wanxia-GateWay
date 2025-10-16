package com.wanxia.gateway.cache.vector;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 向量查询结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryResult {

    /**
     * 相似的文本
     */
    private String text;

    /**
     * 相似文本的向量
     */
    private double[] embedding;

    /**
     * 文本的向量相似度或距离等度量
     */
    private double score;

    /**
     * 相似文本对应的 LLM 生成的回答
     */
    private String answer;
}