package com.wanxia.gateway.cache.vector;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 字符串查询接口
 */
public interface StringQuerier extends Provider {

    /**
     * 查询字符串
     *
     * @param queryString 查询字符串
     * @param callback 回调函数，接收查询结果或错误
     */
    void queryString(String queryString, BiConsumer<List<QueryResult>, Exception> callback);
}