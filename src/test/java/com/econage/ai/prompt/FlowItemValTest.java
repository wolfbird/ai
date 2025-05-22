package com.econage.ai.prompt;

import com.econage.ai.AiDemoApplication;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.chat.ChatFileDTO;
import com.econage.ai.dto.chat.ChatFilterExpressionDTO;
import com.econage.ai.dto.chat.ChatModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;


/**
 * bpma表单自动赋值提示词调试
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowItemValTest extends AbstractPromptTest {

    @Override
    List<Tuple> getUserPrompts() {
        return List.of(
                Tuple.tuple("""
                json格式的内容如下：
                        [{\\"itemId\\":\\"1900079168192552960\\",\\"itemName\\":\\"标题\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079168800727040\\",\\"itemName\\":\\"流程发起大区\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079168918167552\\",\\"itemName\\":\\"招商进度id\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079168981082112\\",\\"itemName\\":\\"创建人Ldap\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170092572672\\",\\"itemName\\":\\"创建人组织\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170180653056\\",\\"itemName\\":\\"项目名称\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170256150528\\",\\"itemName\\":\\"项目编码\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170323259392\\",\\"itemName\\":\\"店铺ID\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170386173952\\",\\"itemName\\":\\"店铺名称\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170453282816\\",\\"itemName\\":\\"店铺号\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170646220800\\",\\"itemName\\":\\"控制层级\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170721718272\\",\\"itemName\\":\\"租户名称\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170788827136\\",\\"itemName\\":\\"经营方式\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170851741696\\",\\"itemName\\":\\"收银方式\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170914656256\\",\\"itemName\\":\\"总承租面积/GLA\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079170969182208\\",\\"itemName\\":\\"建筑总面积/GFA\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079171065651200\\",\\"itemName\\":\\"铺位租金预算\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079171225034752\\",\\"itemName\\":\\"品牌id\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079171304726528\\",\\"itemName\\":\\"品牌名称\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079171376029696\\",\\"itemName\\":\\"合同其他经营品牌\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079171468304384\\",\\"itemName\\":\\"品牌域描述\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079171568967680\\",\\"itemName\\":\\"品牌级次\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079171661242368\\",\\"itemName\\":\\"品牌子级次\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079171728351232\\",\\"itemName\\":\\"品牌业种\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079171812237312\\",\\"itemName\\":\\"品牌子业种\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079172068089856\\",\\"itemName\\":\\"是否有外摆位\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079172114227200\\",\\"itemName\\":\\"位置\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079172185530368\\",\\"itemName\\":\\"外摆位面积\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079172227473408\\",\\"itemName\\":\\"外摆位承租面积\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079172302970880\\",\\"itemName\\":\\"外摆位租凭开始日期\\",\\"itemType\\":\\"日期输入框\\",\\"format\\":\\"yyyy-MM-dd\\",\\"pval\\":null},{\\"itemId\\":\\"1900079172344913920\\",\\"itemName\\":\\"外摆位租凭结束日期\\",\\"itemType\\":\\"日期输入框\\",\\"format\\":\\"yyyy-MM-dd\\",\\"pval\\":null},{\\"itemId\\":\\"1900079172583989248\\",\\"itemName\\":\\"合同模式\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079172634320896\\",\\"itemName\\":\\"合同类型\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079172726595584\\",\\"itemName\\":\\"装修期类型\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079172781121536\\",\\"itemName\\":\\"装修期\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079172885979136\\",\\"itemName\\":\\"店铺预计开业日期\\",\\"itemType\\":\\"日期输入框\\",\\"format\\":\\"yyyy-MM-dd\\",\\"pval\\":null},{\\"itemId\\":\\"1900079172940505088\\",\\"itemName\\":\\"租赁日期\\",\\"itemType\\":\\"日期输入框\\",\\"format\\":\\"yyyy-MM-dd\\",\\"pval\\":null},{\\"itemId\\":\\"1900079173041168384\\",\\"itemName\\":\\"业务审批单号\\",\\"itemType\\":\\"单行输入框\\",\\"format\\":null,\\"pval\\":null},{\\"itemId\\":\\"1900079173083111424\\",\\"itemName\\":\\"合同风险评估意见\\",\\"itemType\\":\\"多行输入框\\",\\"format\\":null,\\"pval\\":null}]
                        """, "")
        );
    }

    @ParameterizedTest
    @MethodSource("parameter")
    void test(ChatModelInfo model, String message, String except) {
        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setChatModelInfo(model);
        chatCompletionRequest.setSystemText("""
                你是一名优秀的数据匹配和结构化输出专家，用户将会提供一段是json对象数组格式的文本，例如[{itemId:"1",itemName:"单选",itemType:"单行输入框",format:"格式化", pval:""},...], 用户上传的json格式文本解释如下：
                    1.itemId为主键，需要在输出中填充到key中;
                    2.itemName为组件的名称，用来分析匹配上传文件的内容;
                    3.itemType为组件的类型，共有单行输入框,多行输入框,富文本输入框,数字输入框,日期输入框,下拉选框,单选框,多选框,级联选择框,说明文字等类型,根据类型的不同，取值的格式会有差异。
                    4.format为这个组件值的格式化形式,主要应用在日期类型的组件中，如果不为空则转换成format值的格式，如果为空则默认转换成YYYY-mm-dd HH:MM:SS格式。
                    5.pval为这个组件可能的值.
                请解析上传文件内容，可以去掉"\\n"、"\\t"、"\\r"这些特殊字符来辅助你理解内容，也可以按"\\n"、"\\t"、"\\r"分割内容后分词理解, 最终提取内容中映射关系, 针对用户提供的json数组依次遍历，获取该对象，在映射关系中进行匹配，如果有匹配到的内容，然后根据对象的itemType进行匹配，将匹配到的值新增为itemValue节点到改json对象中
                匹配规则具体如下：
                    1. 如果对象的itemType是单行输入框,多行输入框,富文本输入框,数字输入框类型，直接将上传文件内容匹配出来的结果填充到输出对象的val中，
                    2. 如果对象的itemType是日期输入框，则需要先识别出来的结果，然后判断json对象中format字段是否有数据，有的话转换成format值的格式，否则默认转换成YYYY-mm-dd HH:MM:SS格式，
                    3. 如果对象的itemType是下拉选框或者单选框，需要按照json对象中pval对象进行匹配，其pval值是[{name:"",val:""},....]结构的字符串数组，将匹配到的文本和pval中的name再次进行匹配：
                        3.1. 按完全匹配＞父级包含＞首个匹配的优先级选择
                        3.2. 必须执行name-val映射转换
                        3.3. 严格禁止输出原始文本
                        3.4. 当文本中出现多个匹配项时, 请随机选择一个
                    4. 如果没有匹配到内容，则不进行输出
                最后不需要额外解释,去掉每个json对象中的pval属性,只输出新增节点后的json格式文本,
                输出前请执行最终校验：
                    1. 输出中的itemId不是自己生成的，必须是用户上传的json文本中对象的itemId,并进行长度一致的检查
                    2. 输出中的itemName必须是用户上传的json文本中对象的itemName
                    3. 下拉选框,单选框的val必须是单个值
                    4. 下拉选框,单选框,多选框必须完成文本到val的转换
                """);

        chatCompletionRequest.setConversationId(UUID.randomUUID().toString());
        chatCompletionRequest.setMessageId(UUID.randomUUID().toString());
        chatCompletionRequest.setMaxTokens(4096L);
        chatCompletionRequest.setWithVectorStore(false);
        ChatFileDTO chatFileDTO = new ChatFileDTO();
        chatFileDTO.setFileTextContent("\t合同模式\n\t其他模式\n\t合同类型\n\t其他类型\n\n装修期类型\n\t其他\n\t装修期\n\t12个月\n\n店铺预计开业日期\n\t2025-03-15\n\t租赁日期\n\t2025-03-01\n\n");
        chatCompletionRequest.setFileTextList(List.of(chatFileDTO));

        chatCompletionRequest.setQueryMessage(message);
        var assistantMessage = integrateFacadeService.chat(chatCompletionRequest);
        var result = assistantMessage.getContent();
        log.info(result);
        Assertions.assertNotNull(assistantMessage.getContent());
    }


}
