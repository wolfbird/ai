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
 * bpa生成流程图提示词调试
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowGraphBPATest extends AbstractPromptTest {

    @Override
    List<Tuple> getUserPrompts() {
        return List.of(
                Tuple.tuple("帮我创建一个请假流程的流程图，员工提交请假申请-部门主管审批-人事审批-员工确认请假时间，若请假时间大于等于三天则需要总经理审批后再流转给人事审批", """
                        <mxGraphModel dx="2177" dy="804" grid="1" guidesSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="0" pageScale="1" pageWidth="827" pageHeight="1169" background="#ffffff">
                        """),
                Tuple.tuple("帮我绘制一个汽车轮胎制造流程", """
                        <mxGraphModel dx="2177" dy="804" grid="1" guidesSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="0" pageScale="1" pageWidth="827" pageHeight="1169" background="#ffffff">
                        """),
                Tuple.tuple("帮我生成一张采购流程图", """
                        <mxGraphModel dx="2177" dy="804" grid="1" guidesSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="0" pageScale="1" pageWidth="827" pageHeight="1169" background="#ffffff">
                        """)
        );
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void test(ChatModelInfo model, String message, String except) {
        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setChatModelInfo(model);
        chatCompletionRequest.setSystemText(modelDescription + """
                我正在使用一款只支持mxgraph格式（xml）的流程图编辑器，帮我返回符合要求的xml，必须完全遵守下面所有规则
                
                # mxgraph流程图生成规则
                
                ## 示例mxgraph格式
                
                ```xml
                
                <mxGraphModel dx="1989" dy="1553" grid="0" gridSize="1" contrast="0" guides="1" tooltips="1" connect="1" arrows="1"
                              fold="1" page="0" pageScale="1" pageWidth="827" pageHeight="1169">
                    <root>
                        <mxCell id="0"/>
                        <mxCell id="1" parent="0"/>
                        <!--主泳道-->
                        <mxCell id="7" value="采购流程" parent="1" vertex="1" compType="ecoBpmn.swimHorizontal">
                            <mxGeometry width="2000" height="640" as="geometry"/>
                        </mxCell>
                        <!--横向泳道-->
                        <mxCell id="8" value="申请人" parent="7" vertex="1" compType="ecoBpmn.swimlane">
                            <mxGeometry y="40" width="2000" height="200" as="geometry"/>
                        </mxCell>
                        <mxCell id="9" value="行政" parent="7" vertex="1" compType="ecoBpmn.swimlane">
                            <mxGeometry y="240" width="2000" height="200" as="geometry"/>
                        </mxCell>
                        <mxCell id="10" value="供应商" parent="7" vertex="1" compType="ecoBpmn.swimlane">
                            <mxGeometry y="440" width="2000" height="200" as="geometry"/>
                        </mxCell>
                        <!--横向泳道内图形-->
                        <mxCell id="14" value="开始" parent="8" vertex="1" compType="ecoBpmn.start">
                            <mxGeometry x="200" y="60" width="120" height="40" as="geometry"/>
                        </mxCell>
                        <mxCell id="36" value="采购寻源" parent="8" vertex="1" compType="ecoBpmn.activity">
                            <mxGeometry x="400" y="60" width="120" height="80" as="geometry"/>
                        </mxCell>
                        <mxCell id="15" value="采购申请" parent="8" vertex="1" compType="ecoBpmn.activity">
                            <mxGeometry x="600" y="60" width="120" height="80" as="geometry"/>
                        </mxCell>
                        <mxCell id="17" value="采购执行" parent="9" vertex="1" compType="ecoBpmn.activity">
                            <mxGeometry x="800" y="60" width="120" height="80" as="geometry"/>
                        </mxCell>
                        <mxCell id="35" value="价格是否合理" parent="9" vertex="1" compType="ecoBpmn.decision">
                            <mxGeometry x="1000" y="60" width="120" height="80" as="geometry"/>
                        </mxCell>
                        <mxCell id="19" value="合同签订" parent="9" vertex="1">
                            <mxGeometry x="1200" y="60" width="120" height="80" as="geometry"/>
                        </mxCell>
                        <mxCell id="21" value="供应商发货" parent="10" vertex="1" compType="ecoBpmn.activity">
                            <mxGeometry x="1400" y="60" width="120" height="80" as="geometry"/>
                        </mxCell>
                        <mxCell id="23" value="到货验收" parent="8" vertex="1" compType="ecoBpmn.activity">
                            <mxGeometry x="1600" y="60" width="120" height="80" as="geometry"/>
                        </mxCell>
                        <mxCell id="30" value="结束" parent="8" vertex="1" compType="ecoBpmn.end">
                            <mxGeometry x="1800" y="60" width="120" height="40" as="geometry"/>
                        </mxCell>
                        <!--连线-->
                        <mxCell id="39" parent="8" source="14" target="36" edge="1">
                            <mxGeometry relative="1" as="geometry"/>
                        </mxCell>
                        <mxCell id="40" parent="8" source="36" target="15" edge="1">
                            <mxGeometry relative="1" as="geometry"/>
                        </mxCell>
                        <mxCell id="41" parent="7" source="15" target="17" edge="1">
                            <mxGeometry relative="1" as="geometry"/>
                        </mxCell>
                        <mxCell id="42" parent="9" source="17" target="35" edge="1">
                            <mxGeometry relative="1" as="geometry"/>
                        </mxCell>
                        <mxCell id="44" value="否" parent="7" source="35" target="36" edge="1">
                            <mxGeometry relative="1" as="geometry"/>
                        </mxCell>
                        <mxCell id="43" value="是" parent="9" source="35" target="19" edge="1">
                            <mxGeometry relative="1" as="geometry"/>
                        </mxCell>
                        <mxCell id="22" parent="7" source="19" target="21" edge="1">
                            <mxGeometry relative="1" as="geometry"/>
                        </mxCell>
                        <mxCell id="24" parent="7" source="21" target="23" edge="1">
                            <mxGeometry relative="1" as="geometry"/>
                        </mxCell>
                        <mxCell id="31" parent="8" source="23" target="30" edge="1">
                            <mxGeometry relative="1" as="geometry"/>
                        </mxCell>
                    </root>
                </mxGraphModel>
                ```
                
                ## mxCell自定义属性compType解释
                
                图形类型由compType属性控制
                
                1. compType="ecoBpmn.swimHorizontal" 表示主泳道，只有一个，描述一条流程
                2. compType="ecoBpmn.swimlane" 表示横向泳道，parent必须是主泳道的id，横向泳道按照角色划分成多个
                3. compType="ecoBpmn.start" 表示开始节点，有且只有一个，value属性固定为开始，表示流程起点，parent是所在横向泳道id
                4. compType="ecoBpmn.end" 表示结束节点，有且只有一个，value属性固定为结束，表示流程办理完成，parent是所在横向泳道id
                5. compType="ecoBpmn.activity" 表示流程审批节点，parent是所在横向泳道id
                6. compType="ecoBpmn.decision" 表示判断条件，根据业务情况生成，parent是所在横向泳道id
                
                ## mxCell属性source、target解释
                
                这两个属性表示流向关系，即连线
                
                1. source表示流入的图形的id,target表示流出的图形的id
                2. **source和target的图形的compType只能是"ecoBpmn.activity"、"ecoBpmn.decision"、"ecoBpmn.start"、"ecoBpmn.end"**
                3. compType="ecoBpmn.activity"和compType="ecoBpmn.decision"的id在source和target**至少都出现一次**，即这些图形必须要有连线流入和流出
                4. compType="ecoBpmn.start"的id至少在source出现一次，可以出现在target，即这种图形必须要有流出，可以有流入
                5. compType="ecoBpmn.end"的id至少在target出现一次，不能出现在source中，即这种图形必须要有流入，不能有流出
                6. 两个图形**不能有双向连线**，即有source="a"和target="b"的情况下是不能有source="b"和target="a"的
                
                ## 横向泳道x,y坐标规则
                
                1. 不需要x坐标
                2. 所有横向泳道y加起来加上40等于主泳道的height
                
                ## 横向泳道内图形x,y坐标规则
                
                1. x,y则是相对于泳道左上角的坐标，
                2. x以200递增
                3. 图形相对泳道要居中，y=(`泳道height`-`图形height`)/2
                4. **y绝对不能超过parent图形的height**，否则流程图会错乱
                
                ## 输出要求
                
                1. **id不能重复，value不能重复**
                2. **不需要任何解释和注释**
                3. **输出必须是纯净的xml格式**
                4. **反复检查输出满足要求**
                """);

        chatCompletionRequest.setConversationId("unit test");
        chatCompletionRequest.setMessageId("unit test");
        chatCompletionRequest.setMaxTokens(4096L);
        chatCompletionRequest.setWithVectorStore(false);
        chatCompletionRequest.setQueryMessage(chat(model, message));
        var assistantMessage = integrateFacadeService.chat(chatCompletionRequest);
        var result = assistantMessage.getContent();
        log.info(result);
        Assertions.assertTrue(assistantMessage.getContent().contains(except));
    }


}
