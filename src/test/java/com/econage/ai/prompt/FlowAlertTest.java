package com.econage.ai.prompt;

import com.econage.ai.AiDemoApplication;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.chat.ChatFilterExpressionDTO;
import com.econage.ai.dto.chat.ChatModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.econage.ai.base.vectorstore.filter.Filter;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * 流程风险预警提示词调试
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowAlertTest extends AbstractPromptTest {

    @Override
    List<Tuple> getUserPrompts() {
        return List.of(
                Tuple.tuple("这条流程有问题吗", "")
        );
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void test(ChatModelInfo model, String message, String except) {
        var flowMessage = """
                流程名称：事假流程
                发起时间：2025-03-03
                请假时间：2025-03-03到2025-03-07
                """;
        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setChatModelInfo(model);
        chatCompletionRequest.setSystemText(modelDescription + """
                用户正在审批流程，请根据上述信息提示风险，提供办理意见，是否批准
                流程信息如下：
                %s
                """.formatted(flowMessage));

        chatCompletionRequest.setConversationId("unit test");
        chatCompletionRequest.setMessageId("unit test");
        chatCompletionRequest.setMaxTokens(4096L);
        chatCompletionRequest.setSimilarityThreshold(0.1);
        chatCompletionRequest.setTopK(10);
        chatCompletionRequest.setWithVectorStore(true);
        List<ChatFilterExpressionDTO> filterExpressionList = Lists.newArrayList();
        var expression = new ChatFilterExpressionDTO();
        expression.setExpressionType(Filter.ExpressionType.IN);
        expression.setKey("docDatasource");
        expression.setValue(List.of("KNOWLEDGE_CENTER"));
        filterExpressionList.add(expression);
        chatCompletionRequest.setFilterExpression(filterExpressionList);

        chatCompletionRequest.setQueryMessage(message);
        var assistantMessage = integrateFacadeService.chat(chatCompletionRequest);
        var result = assistantMessage.getContent();
        log.info(result);
        Assertions.assertNotNull(assistantMessage.getContent());
    }


}
