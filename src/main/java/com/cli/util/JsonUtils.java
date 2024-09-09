package com.cli.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> loadWordTree(String filePath) throws IOException {
        try (InputStream inputStream = JsonUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            Map<String, Object> map = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});

            return map;
        }
    }
}
