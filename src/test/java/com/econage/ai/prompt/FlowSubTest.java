package com.econage.ai.prompt;

import com.econage.ai.AiDemoApplication;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.chat.ChatModelInfo;
import com.econage.ai.dto.vectorstore.VectorStorePushRequest;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;


/**
 * 启动未启动的流程提示词调试
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowSubTest extends AbstractPromptTest {
    private final VectorStorePushRequest push = new VectorStorePushRequest();

    @BeforeAll
    void vector() {
        List<VectorStorePushRequest.VectorStorePushUnion> unions = new ArrayList<>();
        //生成无关数据
        for (int i = 0; i < 5; i++) {
            unions.add(vectorStorePushUnion(Long.toHexString(1231231213L * (i + 11)),
                    String.valueOf(i * 1234578), "WORKFLOW_TEMPLATE"));
        }
        unions.add(vectorStorePushUnion("""
                流程id: 2ggerh423423423
                流程名称: 店铺转让流程
                流程说明:
                流程自定义标识: sgsdg
                流程模板序列号: 234dfhdf
                子流程: 店铺移交、租户接场、装修图纸审批
                """, "2ggerh423423423", "WORKFLOW_TEMPLATE"));
        unions.add(vectorStorePushUnion("""
                流程id: h34234gsdg23
                流程名称: 租户接场
                流程说明: 此流程用于员工因病假等情况申请请假，由直属领导审批后，通知人事部门备案。
                流程自定义标识: a2fsdf
                流程模板序列号: 53463734
                子流程:
                """, "h34234gsdg23", "WORKFLOW_TEMPLATE"));
        unions.add(vectorStorePushUnion("""
                流程id: 5keush354345
                流程名称: 装修图纸审批
                流程说明: 确保装修活动符合城市规划和建筑法规，不改变建筑结构或用途。
                流程自定义标识: 123fdsh3
                流程模板序列号: 97808756756
                子流程:
                """, "5keush354345", "WORKFLOW_TEMPLATE"));
        unions.add(vectorStorePushUnion("""
                流程id: asdas32fasf
                流程名称: 店铺移交
                流程说明:
                流程自定义标识: n34h23h
                流程模板序列号: asdavsf
                子流程:
                """, "asdas32fasf", "WORKFLOW_TEMPLATE"));
        push.setUnions(unions);
        log.info("写入向量数据 {}", push);
        integrateFacadeService.updateVectorStore(push);
    }

    @AfterAll
    void deleteVector() {
        if (push.getUnions() != null) {
            log.info("删除向量数据");
            integrateFacadeService.deleteVectorStore(push.getUnions().stream().map(VectorStorePushRequest.VectorStorePushUnion::getModularInnerId).toList());
        }
    }


    @Override
    List<Tuple> getUserPrompts() {
        return List.of(
                Tuple.tuple("店铺已经移交TC，还缺哪些工作？我应该发起什么流程？", "h34234gsdg23")
        );
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void test(ChatModelInfo model, String message, String except) {
        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setChatModelInfo(model);
        chatCompletionRequest.setSystemText(modelDescription);

        chatCompletionRequest.setConversationId("unit test");
        chatCompletionRequest.setMessageId("unit test");
        chatCompletionRequest.setMaxTokens(4096L);
        chatCompletionRequest.setSimilarityThreshold(0.4);
        chatCompletionRequest.setWithVectorStore(true);
        chatCompletionRequest.setQueryMessage(message);
        var assistantMessage = integrateFacadeService.chat(chatCompletionRequest);
        var result = assistantMessage.getContent().replaceAll("\\r", "")
                .replaceAll("\\n", "")
                .replaceAll(" ", "");
        log.info(result);
        Assertions.assertNotNull(assistantMessage.getContent());
    }


}
