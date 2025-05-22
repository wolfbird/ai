package com.econage.ai.controller;

import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.vectorstore.VectorStorePushRequest;
import com.econage.ai.facade.AIIntegrateFacadeService;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * @author hanpeng
 * @date 2024/9/24 14:49
 */
@RestController
@RequestMapping( "/ai/chat")
public class AIChatController {

    private AIIntegrateFacadeService integrateFacadeService;

    @Autowired
    void auto(
            AIIntegrateFacadeService openAiIntegrateService
    ){
        this.integrateFacadeService = openAiIntegrateService;
    }

    /**
     *
     * @param inputDTO
     * @return
     */
    @PostMapping(value =  "/messages")
    public Object chat(@RequestBody ChatCompletionRequest inputDTO) {
        return integrateFacadeService.chat(inputDTO);
    }


    /**
     *
     * @param inputDTO
     * @return
     */
    @PostMapping(value =  "/completions", produces = "text/event-stream")
    public Object chatCompletions(@RequestBody ChatCompletionRequest inputDTO ) {
        return integrateFacadeService.chatStream(inputDTO);
    }

    /**
     *
     * @param messageId
     * @return
     */
    @GetMapping(value =  "/query/message-document")
    public Object getDocument( @RequestParam String messageId){
        return integrateFacadeService.getMessageDocuments(messageId);
    }





}
