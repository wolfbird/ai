package com.econage.ai.dto.chat;

import lombok.Data;
import org.springframework.ai.chat.messages.MessageType;

/**
 * @author hanpeng
 * @date 2025/2/11 18:23
 */
@Data
public class ChatMemoryDTO {

    private MessageType messageType;

    private String content;
}
