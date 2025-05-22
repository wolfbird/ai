package com.econage.ai.prompt;

import com.econage.ai.AiDemoApplication;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.chat.ChatFilterExpressionDTO;
import com.econage.ai.dto.chat.ChatModelInfo;
import com.econage.ai.dto.vectorstore.VectorStorePushRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.econage.ai.base.vectorstore.filter.Filter;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;


/**
 * 启动流程提示词调试
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowStartTest extends AbstractPromptTest {
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
                流程id: 1784583510566940673
                流程名称: 通用请假申请流程
                流程说明: 此流程用于通用情况申请请假，由直属领导审批后，通知人事部门备案
                流程自定义标识: sss123
                流程模板序列号: 12
                子流程: 租户接场、装修图纸审批
                """, "1151565161651651", "WORKFLOW_TEMPLATE"));
        unions.add(vectorStorePushUnion("""
                流程id: F12345
                流程名称: 病假申请流程
                流程说明: 此流程用于员工因病假等情况申请请假，由直属领导审批后，通知人事部门备案。
                流程自定义标识: asdaf
                流程模板序列号: 48
                子流程: 租户接场、装修图纸审批
                """, "987621", "WORKFLOW_TEMPLATE"));
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
                Tuple.tuple("生病了，想要启动一个请假流程", "F12345"),
                Tuple.tuple("启动一个请假流程", "1784583510566940673"),
                Tuple.tuple("启动一个用车申请流程", "[]"),
                Tuple.tuple("今天天气还行", "[]"),
                Tuple.tuple("启动一个AI测试流程", "1894985913818832897"),
                Tuple.tuple("AI测试", "1894985913818832897"),
                Tuple.tuple("售前演示", "1892038959929835521")
        );
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void test(ChatModelInfo model, String message, String except) {
        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setChatModelInfo(model);
        chatCompletionRequest.setSystemText(modelDescription + """
                用户想启动流程，但是不知道启动哪条，请你从学习到的知识中找到符合要求的流程信息，必须满足以下所有输出要求：
                1、输出必须是JSON数组格式，如果你不清楚学习到的知识是否符合那么返回学习到的知识的第一条，如果学习到的知识中没有流程信息的返回空数组：[]
                2、一条流程数据JSON格式如下：{"id": "流程id","name": "流程名称","comment": "流程说明","defFieldId":"自定义标识","wfSerial":"序列号"}
                3、输出的所有值都只能来自学习到的知识，不能修改，按照相关度返回前5条
                4、不要解释
                5、不允许任何Markdown格式，即输出必须以[开头，以]结尾
                """);

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
        expression.setValue(List.of("WORKFLOW_TEMPLATE"));
        filterExpressionList.add(expression);
        chatCompletionRequest.setFilterExpression(filterExpressionList);

        chatCompletionRequest.setQueryMessage(message);
        var assistantMessage = integrateFacadeService.chat(chatCompletionRequest);
        var result = assistantMessage.getContent().replaceAll("\\r", "")
                .replaceAll("\\n", "")
                .replaceAll(" ", "");
        log.info(result);
        Assertions.assertTrue(result.contains(except), "异常返回");
        Assertions.assertTrue(result.startsWith("["), "异常返回");
        Assertions.assertTrue(result.endsWith("]"), "异常返回");
        if (!except.equals("[]")) {
            Assertions.assertTrue(result.startsWith("[{"), "异常返回");
            Assertions.assertTrue(result.endsWith("}]"), "异常返回");
        }
        Assertions.assertFalse(result.contains("```"), "包含```");
    }


}
