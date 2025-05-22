package com.econage.ai.boot.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author hanpeng
 * @date 2025/2/20 12:37
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary // 让 Spring 使用这个 ObjectMapper 作为默认 Bean
    public ObjectMapper objectMapper() {
        // 配置 StreamReadConstraints
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(50_000_000) // 允许最大字符串长度为 50MB
                .build();

        // 创建 JsonFactory 并应用该约束
        JsonFactory jsonFactory = JsonFactory.builder()
                .streamReadConstraints(constraints)
                .build();

        // 创建并返回自定义的 ObjectMapper
        var objectMapper =   JsonMapper.builder(jsonFactory).build();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

}
