package com.econage.ai.dto.chat;

import lombok.Data;

import java.util.Map;

/**
 * @author hanpeng
 * @date 2025/3/3 15:59
 */
@Data
public class ChatCompletionResponse {

    private Map<String, Object> metadata;

    private String content;
}
