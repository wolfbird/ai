package com.econage.ai.support.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hanpeng
 * @date 2025/3/3 13:51
 */
@Component
@Slf4j
public class ObjectMapperSupport {

    private ObjectMapper objectMapper;

    @Autowired
    void auto(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String writeValueAsString( Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("json operation error {}", object, e);
        }
        return StringUtils.EMPTY;
    }
}
