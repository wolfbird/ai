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
 * bpi数据研报提示词调试
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BPIModelTest extends AbstractPromptTest {

    private final VectorStorePushRequest push = new VectorStorePushRequest();

    @BeforeAll
    void vector() {
        List<VectorStorePushRequest.VectorStorePushUnion> unions = new ArrayList<>();
        //生成无关数据
        for (int i = 0; i < 5; i++) {
            unions.add(vectorStorePushUnion(Long.toHexString(1231231213L * (i + 11)),
                    String.valueOf(i * 1234578), "BPI_MODEL"));
        }
        unions.add(vectorStorePushUnion("""
                id: 178450673
                数据库表: 合同表
                """, "178450673", "BPI_MODEL"));
        unions.add(vectorStorePushUnion("""
                id: 1784sdfsd673
                数据库表: 合同表2
                """, "1784sdfsd673", "BPI_MODEL"));
        unions.add(vectorStorePushUnion("""
                id: 1784586940673
                数据库表: 客户表
                """, "1784586940673", "BPI_MODEL"));
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
                Tuple.tuple("我想知道最近三个月的合同总金额", "[\"178450673\",\"1784sdfsd673\""),
                Tuple.tuple("我想知道最近三个月的客户分布情况", "[\"1784586940673\"")
        );
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void test(ChatModelInfo model, String message, String except) {

        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setChatModelInfo(model);
        chatCompletionRequest.setSystemText(modelDescription + """
                用户想做一些数据分析，但是不知道该用哪些数据库表，请你从学习到的知识中找到符合要求的id，示例：
                    学习到的知识：
                        id: 3456abcd
                        数据库表: 进度表
                    用户提问与“进度表”相关的需要返回id数组：
                        ["3456abcd"]
                必须满足以下所有输出要求：
                1、输出的所有值都只能来自学习到的知识，不能修改，按照表与用户诉求的相关度返回前5条id，格式示例：["id1","id2","id3","id4","id5"]
                2、如果你不清楚学习到的知识是否符合那么返回学习到的知识的第一条，如果学习到的知识中没有id相关信息的返回[]
                3、不要解释
                4、不允许任何Markdown格式，即输出必须以[开头，以]结尾
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
        expression.setValue(List.of("BPI_MODEL"));
        filterExpressionList.add(expression);
        chatCompletionRequest.setFilterExpression(filterExpressionList);

        chatCompletionRequest.setQueryMessage(message);
        var assistantMessage = integrateFacadeService.chat(chatCompletionRequest);
        var result = assistantMessage.getContent();
        log.info(result);
        Assertions.assertTrue(assistantMessage.getContent().replaceAll(" ","").startsWith(except));
        Assertions.assertTrue(assistantMessage.getContent().startsWith("["));
        Assertions.assertTrue(assistantMessage.getContent().endsWith("]"));
    }


}
