package com.econage.ai;

import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.chat.ChatModelInfo;
import com.econage.ai.dto.vectorstore.VectorStorePushRequest;
import com.econage.ai.facade.AIIntegrateFacadeService;
import com.econage.ai.support.meta.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tzl
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {AiDemoApplication.class})
public class SimilaritySearchTest {
    @Autowired
    protected AIIntegrateFacadeService integrateFacadeService;

    private final VectorStorePushRequest push = new VectorStorePushRequest();

    @BeforeAll
    void vector() {
        List<VectorStorePushRequest.VectorStorePushUnion> unions = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            var vectorStorePushUnion = new VectorStorePushRequest.VectorStorePushUnion();
            vectorStorePushUnion.setModularInnerId("unit test " + i);
            vectorStorePushUnion.setDocText("""
                    流程名称: 通用请假申请流程
                    流程id: %s
                    """.formatted(vectorStorePushUnion.getModularInnerId()));
            Map<String, Object> metaDataMap = new HashMap<>();
            metaDataMap.put("source", i + ".txt");
            metaDataMap.put("docDatasource", "UNIT_TEST" + i % 5);
            vectorStorePushUnion.setMetaDataMap(metaDataMap);
            unions.add(vectorStorePushUnion);
        }
        push.setUnions(unions);
        integrateFacadeService.updateVectorStore(push);
    }

    @Test
    void test() {
        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        ChatModelInfo qwenTurbo = new ChatModelInfo();
        qwenTurbo.setModel("qwen-turbo");
        qwenTurbo.setProviderType(ProviderType.DASH_SCOPE);
        qwenTurbo.setApiKey("sk-3df07a6905a04c2f9389569dc9790e95");
        chatCompletionRequest.setChatModelInfo(qwenTurbo);
        chatCompletionRequest.setQueryMessage("""
                找一些请假流程
                """);

        chatCompletionRequest.setConversationId("unit test");
        chatCompletionRequest.setMessageId("unit test");
        chatCompletionRequest.setMaxTokens(4096L);
        chatCompletionRequest.setWithVectorStore(true);
        chatCompletionRequest.setSimilarityThreshold(0.7d);
        chatCompletionRequest.setTopK(21);
        var response = integrateFacadeService.chat(chatCompletionRequest);
        log.info("response {}", response);
        Assertions.assertNotNull(response);
    }

    @AfterAll
    void deleteVector() {
        if (push.getUnions() != null) {
            log.info("删除向量数据");
            integrateFacadeService.deleteVectorStore(push.getUnions().stream().map(VectorStorePushRequest.VectorStorePushUnion::getModularInnerId).toList());
        }
    }
}
