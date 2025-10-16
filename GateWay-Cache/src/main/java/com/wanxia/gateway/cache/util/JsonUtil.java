package com.wanxia.gateway.cache.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtil {
    public static String getStringValue(JsonNode json, String fieldName, String defaultValue) {
        if (json.has(fieldName) && !json.get(fieldName).isNull()) {
            return json.get(fieldName).asText();
        }
        return defaultValue;
    }

    public static int getIntValue(JsonNode json, String fieldName, int defaultValue) {
        if (json.has(fieldName)) {
            return json.get(fieldName).asInt(defaultValue);
        }
        return defaultValue;
    }
}
