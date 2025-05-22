package com.econage.ai.dto.chat;


import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

/**
 * @author hanpeng
 * @date 2025/2/20 10:31
 */
@Data
public class ChatMediaDTO {

    private MediaType mimeType;

    private String base64Content;
}
