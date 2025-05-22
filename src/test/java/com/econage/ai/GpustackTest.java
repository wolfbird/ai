package com.econage.ai;

import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.chat.ChatCompletionResponse;
import com.econage.ai.dto.chat.ChatModelInfo;
import com.econage.ai.facade.AIIntegrateFacadeService;
import com.econage.ai.support.meta.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

/**
 * gpustack 部署的deepseek兼容性测试
 *
 * @author tzl
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GpustackTest {

    @Autowired
    private AIIntegrateFacadeService integrateFacadeService;


    @Test
    void test1() {
        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        ChatModelInfo chatModelInfo = new ChatModelInfo();
        chatModelInfo.setModel("deepseek-r1:32b-1");
        chatModelInfo.setProviderType(ProviderType.OPENAI_COMPATIBLE);
        chatModelInfo.setBaseUrl("http://192.168.0.220/v1-openai");
        chatModelInfo.setApiKey("gpustack_91863687e748e2a5_787198f7e7593000c2c5ed972a8867c2");
        chatCompletionRequest.setChatModelInfo(chatModelInfo);
        chatCompletionRequest.setQueryMessage("""
                你是谁
                """);

        chatCompletionRequest.setConversationId("unit test");
        chatCompletionRequest.setMessageId("unit test");
        chatCompletionRequest.setMaxTokens(4096L);
        chatCompletionRequest.setWithVectorStore(false);
        var stream = (Flux<ChatCompletionResponse>) integrateFacadeService.chatStream(chatCompletionRequest);
        var assistantMessage = stream.blockLast();
        log.info("response {}", assistantMessage);
        Assertions.assertNotNull(assistantMessage);
    }


}
