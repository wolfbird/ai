package com.econage.ai.prompt;

import com.econage.ai.AiDemoApplication;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.chat.ChatModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * bpi数据研报提示词调试
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BPIChartTest extends AbstractPromptTest {

    @Override
    List<Tuple> getUserPrompts() {
        return List.of(
                Tuple.tuple("我想知道最近三个月的合同总金额", """
                        现在有数据表：
                        CREATE TABLE flow_instance_basic_
                        (
                            `REQ_DATA_ID`       String comment '流程实例id',
                            `REQ_STATUS`        String comment '流程状态',
                            STRING_DATA['1'] String comment '合同名称',
                            STRING_DATA['2'] String comment '合同时间',
                            STRING_DATA['3'] String comment '合同金额'
                        ) ENGINE = MergeTree();
                        """),
                Tuple.tuple("我想知道最近三个月的客户分布情况", """
                        现在有数据表：
                        CREATE TABLE customers
                        (
                            customer_id UInt64 COMMENT '客户唯一标识（主键）',
                            name String COMMENT '客户姓名',
                            email String COMMENT '电子邮箱',
                            phone String COMMENT '电话号码',
                            province LowCardinality(String) COMMENT '客户所在省份',
                            address String COMMENT '详细地址',
                            registration_date Date COMMENT '注册日期'
                        )
                        ENGINE = MergeTree();
                        """)
        );
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void test(ChatModelInfo model, String message, String except) {

        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setChatModelInfo(model);
        chatCompletionRequest.setSystemText(modelDescription + """
                 你是一个商业智能BI助手，功能是根据给出的ClickHouse数据库表生成有业务价值的图表。例如对于数据表：
                 CREATE TABLE flow_instance_basic_
                 (
                     `REQ_DATA_ID`       String comment '流程实例id',
                     `REQ_STATUS`        String comment '流程状态 0进行中 1已完成',
                     STRING_DATA['2'] String comment '项目类型',
                     STRING_DATA['7'] String comment '项目金额',
                     STRING_DATA['11'] String comment '项目结束时间'
                 ) ENGINE = MergeTree();
                
                用户要求统计近半年流程状态为已完成各类型项目金额分布，则需要输出一个JSON对象：
                {
                 "sql": "select STRING_DATA['2'] as `项目类型`,sum(toFloat64OrZero(toString(STRING_DATA['7']))) as `项目金额合计`,count(*) as `项目合计` from flow_instance_basic_ where parseDateTime64BestEffortOrNull(STRING_DATA['11']) > addMonths(now(), -6) and `REQ_STATUS` = '1' group by STRING_DATA['2']",
                 "dimensions": ["项目类型"],
                 "quotas": ["项目金额合计","项目合计"],
                 "title": "最近半年不同类型项目金额分布"
                }
                 JSON输出还需要满足下列所有要求：
                 1、sql字段表示需要执行的clickhouse sql；dimensions表示sql中统计维度（被group by的字段）；quotas表示统计的指标，不能为空，默认使用count()表示计数
                 2、要注意`的使用。如果建表语句中字段包含`那么生成的sql中使用到该字段的也要加上`；如果建表语句中不包含`的那么生成的sql中使用到该字段的不能加上`；sql返回的字段要使用as给别名，别名都要加上`
                 3、dimensions和quotas数组的长度不超过2个，具体按照用户业务需求确定，quotas不能为空
                 4、你不知道怎么统计或者给不出可执行的clickhouse sql，请返回空对象：{}
                 5、不要注释和解释
                 6、绝对不允许```json```等Markdown格式包裹
                """ + except);

        chatCompletionRequest.setConversationId("unit test");
        chatCompletionRequest.setMessageId("unit test");
        chatCompletionRequest.setMaxTokens(4096L);
        chatCompletionRequest.setWithVectorStore(false);
        chatCompletionRequest.setQueryMessage(message);
        var assistantMessage = integrateFacadeService.chat(chatCompletionRequest);
        var result = assistantMessage.getContent();
        log.info(result);
        Assertions.assertTrue(assistantMessage.getContent().replaceAll(" ", "").replaceAll("\\r", "").replaceAll("\\n", "").startsWith("{\"sql\":"));
        Assertions.assertTrue(assistantMessage.getContent().endsWith("}"));
    }


}
