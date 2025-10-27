package com.wanxia.gateway.tokenratelimit.util;

import com.alibaba.nacos.common.utils.StringUtils;
import com.wanxia.gateway.core.request.GatewayRequest;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Proxy-Wasm 工具类，用于处理请求头、IP、Cookie 等相关操作
 */
public class ProxyIpUtil {

    /**
     * Consumer 请求头常量，用于从 HTTP 请求头中获取 consumer 的名字
     */
    public static final String CONSUMER_HEADER = "x-mse-consumer";

    /**
     * 解析 IP 段配置
     * 注：Java 版本需要使用第三方库如 commons-net 或自定义实现
     * 这里提供基础的 IP 段验证逻辑
     *
     * @param ipRange IP 段配置，如 "192.168.1.0/24"
     * @return 是否为有效的 IP 段
     * @throws IllegalArgumentException 如果 IP 段格式无效
     */
    public static boolean parseIPNet(String ipRange) {
        if (StringUtils.isBlank(ipRange)) {
            throw new IllegalArgumentException("IP range cannot be blank");
        }

        // 验证 CIDR 格式
        if (!ipRange.contains("/")) {
            throw new IllegalArgumentException(String.format("Invalid IP range format[%s]", ipRange));
        }

        String[] parts = ipRange.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException(String.format("Invalid IP range format[%s]", ipRange));
        }

        String ip = parts[0];
        String mask = parts[1];

        // 验证 IP 地址格式
        if (!isValidIP(ip)) {
            throw new IllegalArgumentException(String.format("Invalid IP[%s]", ip));
        }

        // 验证掩码
        try {
            int maskBits = Integer.parseInt(mask);
            if (maskBits < 0 || maskBits > 32) {
                throw new IllegalArgumentException(String.format("Invalid IP mask[%s]", mask));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid IP mask[%s]", mask));
        }

        return true;
    }

    /**
     * 验证 IP 地址格式（IPv4 或 IPv6）
     *
     * @param ip IP 地址
     * @return 是否为有效的 IP 地址
     */
    private static boolean isValidIP(String ip) {
        // IPv4 验证
        if (ip.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) {
            String[] parts = ip.split("\\.");
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        }

        // IPv6 验证（简单检查）
        if (ip.contains(":")) {
            return true;
        }

        return false;
    }

    /**
     * 解析 IP 地址，从包含端口的字符串中提取 IP
     * 支持 IPv4 和 IPv6 格式
     *
     * @param source 源字符串，可能包含端口号
     * @return 提取的 IP 地址
     */
    public static String parseIP(String source) {
        if (StringUtils.isBlank(source)) {
            return source;
        }

        // 处理 IPv4 格式 (IP:port)
        if (source.contains(".")) {
            return source.split(":")[0];
        }

        // 处理 IPv6 格式 ([IPv6]:port)
        if (source.contains("]")) {
            return source.split("]")[0].substring(1);
        }

        return source;
    }

    /**
     * 将 HttpHeaders 转换为排序的键值对数组
     * 将 Map<String, List<String>> 格式转换为 String[][] 格式
     *
     * @param headers HTTP 请求头，格式为 Map<String, List<String>>
     * @return 排序后的键值对数组
     */
    public static String[][] reconvertHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return new String[0][2];
        }

        List<String[]> headerList = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            if (values != null) {
                for (String value : values) {
                    headerList.add(new String[]{key, value});
                }
            }
        }

        // 按键名排序
        headerList.sort((a, b) -> a[0].compareTo(b[0]));

        return headerList.toArray(new String[0][2]);
    }

    /**
     * 从 Netty HttpHeaders 转换为排序的键值对数组
     *
     * @param headers Netty HttpHeaders 对象
     * @return 排序后的键值对数组
     */
    public static String[][] reconvertHeaders(HttpHeaders headers) {
        if (headers == null || headers.isEmpty()) {
            return new String[0][2];
        }

        List<String[]> headerList = new ArrayList<>();

        for (Map.Entry<String, String> entry : headers) {
            headerList.add(new String[]{entry.getKey(), entry.getValue()});
        }

        // 按键名排序
        headerList.sort((a, b) -> a[0].compareTo(b[0]));

        return headerList.toArray(new String[0][2]);
    }

    /**
     * 从 Cookie 字符串中提取指定 key 对应的 value
     *
     * @param cookie Cookie 字符串，格式为 "key1=value1; key2=value2"
     * @param key    要提取的 key
     * @return 对应的 value，如果不存在则返回空字符串
     */
    public static String extractCookieValueByKey(String cookie, String key) {
        if (StringUtils.isBlank(cookie) || StringUtils.isBlank(key)) {
            return "";
        }

        String[] pairs = cookie.split(";");
        for (String pair : pairs) {
            pair = pair.trim();
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }

        return "";
    }

    /**
     * 获取路由名称
     * 从请求上下文中获取路由名称
     *
     * @param gatewayRequest 网关请求对象
     * @return 路由名称，如果获取失败返回 "-"
     */
    public static String getRouteName(GatewayRequest gatewayRequest) {
        try {
            // 从请求属性中获取路由名称
            Object routeName = gatewayRequest.getAttribute("route_name");
            return routeName != null ? routeName.toString() : "-";
        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * 获取集群名称
     * 从请求上下文中获取集群名称
     *
     * @param gatewayRequest 网关请求对象
     * @return 集群名称，如果获取失败返回 "-"
     */
    public static String getClusterName(GatewayRequest gatewayRequest) {
        try {
            // 从请求属性中获取集群名称
            Object clusterName = gatewayRequest.getAttribute("cluster_name");
            return clusterName != null ? clusterName.toString() : "-";
        } catch (Exception e) {
            return "-";
        }
    }

    /**
     * 获取 Consumer 名称
     * 从 HTTP 请求头中获取 consumer 的名字
     *
     * @param headers HTTP 请求头
     * @return Consumer 名称，如果获取失败返回 "none"
     */
    public static String getConsumer(HttpHeaders headers) {
        try {
            if (headers == null) {
                return "none";
            }
            String consumer = headers.get(CONSUMER_HEADER);
            return StringUtils.isNotBlank(consumer) ? consumer : "none";
        } catch (Exception e) {
            return "none";
        }
    }

    /**
     * 获取 Consumer 名称（从 Map 格式的请求头中）
     *
     * @param headers 请求头 Map，格式为 Map<String, List<String>>
     * @return Consumer 名称，如果获取失败返回 "none"
     */
    public static String getConsumer(Map<String, List<String>> headers) {
        try {
            if (headers == null) {
                return "none";
            }
            List<String> consumerList = headers.get(CONSUMER_HEADER);
            if (consumerList != null && !consumerList.isEmpty()) {
                String consumer = consumerList.get(0);
                return StringUtils.isNotBlank(consumer) ? consumer : "none";
            }
            return "none";
        } catch (Exception e) {
            return "none";
        }
    }
}
