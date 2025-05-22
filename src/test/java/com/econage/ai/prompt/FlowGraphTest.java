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
 * 生成流程图提示词调试
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowGraphTest extends AbstractPromptTest {

    @Override
    List<Tuple> getUserPrompts() {
        return List.of(
                Tuple.tuple("帮我创建一个请假流程的流程图，员工提交请假申请-部门主管审批-人事审批-员工确认请假时间，若请假时间大于等于三天则需要总经理审批后再流转给人事审批", """
                        <mxGraphModel dx="2177" dy="804" grid="1" guidesSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="0" pageScale="1" pageWidth="827" pageHeight="1169" background="#ffffff">
                        """),
                Tuple.tuple("帮我创建一个流程图，以下是各个节点的信息： 环节名称 执行角色 职责描述 上一环节 1. 工单创建 用户 提交工单，填写问题描述、上传附件 - 2. 工单分类 AI机器人 通过NLP分析问题描述，自动分类（退货/物流/质量等） 工单创建 3. 优先级分配 AI机器人 根据用户等级（VIP/普通）和问题紧急程度分配优先级（S0/S1/S2） 工单分类 4. 参考意见生成 AI机器人 检索历史工单，生成处理建议（如“同意退货”） 优先级分配 5. 客服分配 AI机器人 根据技能匹配和工作负载，分配工单给对应客服 参考意见生成 6. 工单入库 系统 将工单存储至数据库，更新状态为“待处理” 参考意见生成 7. 任务处理 客服人员 处理工单： - 普通任务：直接解决 - 高风险任务：提交审批 - 重复任务：合并处理 参考意见生成 8. 高风险审批 审批主管 审核高风险工单，填写审批意见（如“需补充证明材料”） 任务处理（高风险分支） 9. 处理反馈 客服人员 填写最终解决方案（如“已退款”）并关闭工单 任务处理/高风险审批 10. 工单关闭 系统 更新工单状态为“已解决”，触发用户评价 处理反馈 11. 用户评价 用户 对处理结果进行满意度评分（1-5星） 工单关闭", """
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
                我正在设计审批流程，根据要求给出mxgraph格式流程图（xml）
                
                # mxgraph流程图生成规则
                
                ## 示例mxgraph格式
                
                ```xml
                
                <mxGraphModel dx="2177" dy="804" grid="1" guidesSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1"
                              page="0" pageScale="1" pageWidth="827" pageHeight="1169" background="#ffffff">
                    <root>
                        <mxCell id="0"/>
                        <mxCell id="1" parent="0"/>
                        <!--节点-->
                        <mxCell id="10" parent="1" value="开始" vertex="1" taskLevel="1" compType="bpma.common.startTask">
                            <mxGeometry as="geometry" width="60" height="60" x="0.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="11" parent="1" value="组长审批" vertex="1" taskLevel="2" compType="bpma.common.workTask">
                            <mxGeometry as="geometry" width="60" height="60" x="150.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="12" parent="1" value="评估级别" vertex="1" taskLevel="3" compType="bpma.common.conditionTask">
                            <mxGeometry as="geometry" width="60" height="60" x="300.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="13" parent="1" value="经理审批" vertex="1" taskLevel="4" compType="bpma.common.workTask">
                            <mxGeometry as="geometry" width="60" height="60" x="450.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="14" parent="1" value="主管审批" vertex="1" taskLevel="5" compType="bpma.common.workTask">
                            <!--如果是分支的，x一样,y不一样-->
                            <mxGeometry as="geometry" width="60" height="60" x="450.0" y="150.0"/>
                        </mxCell>
                        <mxCell id="15" parent="1" value="人事备案" vertex="1" taskLevel="6" compType="bpma.common.workTask">
                            <mxGeometry as="geometry" width="60" height="60" x="600.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="16" parent="1" value="结束环节" vertex="1" taskLevel="7" compType="bpma.common.endTask">
                            <mxGeometry as="geometry" width="60" height="60" x="750.0" y="0.0"/>
                        </mxCell>
                        <!--连线-->
                        <mxCell id="100" parent="1" edge="1" source="10" target="11">
                            <mxGeometry relative="1" as="geometry" x="0.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="101" parent="1" edge="1" source="11" target="12">
                            <mxGeometry relative="1" as="geometry" x="0.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="102" parent="1" edge="1" source="12" target="13">
                            <mxGeometry relative="1" as="geometry" x="0.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="103" parent="1" style="entryX=0;entryY=0.5;" edge="1" source="12" target="14">
                            <mxGeometry relative="1" as="geometry" x="0.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="104" parent="1" edge="1" source="13" target="15">
                            <mxGeometry relative="1" as="geometry" x="0.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="105" parent="1" edge="1" source="14" target="15">
                            <mxGeometry relative="1" as="geometry" x="0.0" y="0.0"/>
                        </mxCell>
                        <mxCell id="106" parent="1" edge="1" source="15" target="16">
                            <mxGeometry relative="1" as="geometry" x="0.0" y="0.0"/>
                        </mxCell>
                    </root>
                </mxGraphModel>
                ```
                
                ## 名称解释
                
                * 节点：具有compType属性的<mxCell>
                * 连线：具有source和target属性的<mxCell>
                * 分支：节点id是在source出现多次，那么这个节点是分支节点，compType="bpma.common.conditionTask"
                
                ## 节点
                
                节点类型由compType属性控制
                
                1. compType="bpma.common.startTask" 表示开始节点，表示流程起点
                2. compType="bpma.common.endTask" 表示结束节点，表示流程办理完成
                3. compType="bpma.common.workTask" 表示流程人工审批节点
                4. compType="bpma.common.conditionTask" 表示分支节点，是自动执行的，不需要人工审批，一般是判断条件，流程分支条件
                5. 增加taskLevel属性并递增
                6. **节点value不能重复**
                7. width和height固定60
                
                ## 连线
                
                属性source、target表示流向关系
                
                1. source表示流入的节点的id,target表示流出的节点的id
                2. **source和target的id只能是节点的id**
                3. compType="bpma.common.workTask"和compType="bpma.common.conditionTask"的id在source和target**至少都出现一次**
                   ，即这些节点必须要有连线流入和流出
                4. 两个节点**不能有双向连线**，即有source="a"和target="b"的情况下是不能有source="b"和target="a"的
                5. 如果提到了节点的前后顺序，那么要按照这个顺序进行连线
                6. 如果是分支条件的连线，style属性中"entryX=0;entryY=0.5;"控制箭头指向目标节点位置，"entryX=0;entryY=0.5;"表示左边，"
                   entryX=1;entryY=0.5;"表示右边，"entryX=0.5;entryY=0;"表示上边，"entryX=0.5;entryY=1;"表示下边
                7. 连线要有固定子属性<mxGeometry relative="1" as="geometry" x="0.0" y="0.0"/>
                
                ## 业务相关要求
                
                1. 第一个节点必须是开始节点，有且只有一个，必须要有流出，可以有流入
                2. 最后一个节点必须是结束节点，有且只有一个，必须要有流入，不能有流出
                3. 提到的节点都要生成人工审批节点，并且按照业务要求按顺序生成
                4. 要控制节点y坐标，分支出去的节点x坐标一样，y坐标不能一样
                
                # 输出要求
                
                1. **id不能重复**
                2. **value不能重复**
                3. **不需要任何解释和注释**
                4. **输出必须是纯净的xml格式**
                5. **反复确认输出满足要求**
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
