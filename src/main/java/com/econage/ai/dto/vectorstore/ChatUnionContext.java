package com.econage.ai.dto.vectorstore;

import lombok.Data;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @author hanpeng
 * @date 2025/2/12 12:06
 */
@Data
public class ChatUnionContext {

    private String content;

    private List<Document> documents;

    private String responseMessageId;
}
