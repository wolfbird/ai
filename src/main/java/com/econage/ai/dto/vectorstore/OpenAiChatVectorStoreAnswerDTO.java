package com.econage.ai.dto.vectorstore;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author hanpeng
 * @date 2024/9/27 13:43
 */
@Data
public class OpenAiChatVectorStoreAnswerDTO {

    /**
     * AI回答的自然语言内容
     */
    private String content;

    /**
     * 回答取自的向量数据的元信息
     */
    private List<Map<String,Object>> responseMetaData;
}
