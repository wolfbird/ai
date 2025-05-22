package com.econage.ai.prompt;

import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.chat.ChatModelInfo;
import com.econage.ai.dto.vectorstore.VectorStorePushRequest;
import com.econage.ai.facade.AIIntegrateFacadeService;
import com.econage.ai.support.meta.ProviderType;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * 提示词调试
 */
abstract class AbstractPromptTest {
    @Autowired
    protected AIIntegrateFacadeService integrateFacadeService;

    protected String modelDescription = "你是微宏科技旗下AlphaFlow业务流程管理平台产品中的AI智能助手。如果用户问到“你是谁？”、“你是基于什么模型？”等类似让你自我介绍的问题，你可以如实回答你底层是基于什么模型，并且同时要带上微宏和AlphaFlow的信息，说明你是AlphaFlow产品的AI智能助手，并且经过微宏技术团队结合业务流程领域知识数据的专项训练，能够为用户提供更精准高效的应答及服务。";


    protected VectorStorePushRequest.VectorStorePushUnion vectorStorePushUnion(String text, String id, String docDatasource) {
        var vectorStorePushUnion = new VectorStorePushRequest.VectorStorePushUnion();
        vectorStorePushUnion.setDocText(text);
        vectorStorePushUnion.setModularInnerId(id);
        Map<String, Object> metaDataMap = new HashMap<>();
        metaDataMap.put("source", id + ".txt");
        metaDataMap.put("docDatasource", docDatasource);
        vectorStorePushUnion.setMetaDataMap(metaDataMap);
        return vectorStorePushUnion;
    }

    private List<ChatModelInfo> getModels() {
        List<ChatModelInfo> list = new ArrayList<>();
        ChatModelInfo qwenTurbo = new ChatModelInfo();
        qwenTurbo.setModel("qwen-turbo");
        qwenTurbo.setProviderType(ProviderType.DASH_SCOPE);
        qwenTurbo.setApiKey("sk-3df07a6905a04c2f9389569dc9790e95");
        list.add(qwenTurbo);
//        ChatModelInfo qwenMax = new ChatModelInfo();
//        qwenMax.setModel("qwen-max");
//        qwenMax.setProviderType(ProviderType.DASH_SCOPE);
//        qwenMax.setApiKey("sk-3df07a6905a04c2f9389569dc9790e95");
//        list.add(qwenMax);
//        ChatModelInfo qwenPlus = new ChatModelInfo();
//        qwenPlus.setModel("qwen-plus");
//        qwenPlus.setProviderType(ProviderType.DASH_SCOPE);
//        qwenPlus.setApiKey("sk-3df07a6905a04c2f9389569dc9790e95");
//        list.add(qwenPlus);
//        ChatModelInfo deepseek = new ChatModelInfo();
//        deepseek.setModel("deepseek-r1:671b");
//        deepseek.setProviderType(ProviderType.OPENAI_COMPATIBLE);
//        deepseek.setBaseUrl("http://192.168.0.220/v1-openai");
//        deepseek.setApiKey("gpustack_99a0de9d8197183c_1ff1030059cdcdd75324201f71a07c91");
//        list.add(deepseek);
        return list;
    }

    abstract List<Tuple> getUserPrompts();

    /**
     * 助手对话模拟
     *
     * @param model   模型
     * @param message 用户输入
     * @return 输出
     */
    protected String chat(ChatModelInfo model, String message) {
        final ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setChatModelInfo(model);
        chatCompletionRequest.setSystemText(modelDescription);

        chatCompletionRequest.setConversationId("unit test");
        chatCompletionRequest.setMessageId("unit test");
        chatCompletionRequest.setMaxTokens(4096L);
        chatCompletionRequest.setWithVectorStore(true);
        chatCompletionRequest.setQueryMessage(message);
        var assistantMessage = integrateFacadeService.chat(chatCompletionRequest);
        return assistantMessage.getContent();
    }

    protected Stream<Arguments> parameter() {
        List<Arguments> list = new ArrayList<>();
        for (ChatModelInfo model : getModels()) {
            for (Tuple userPrompt : getUserPrompts()) {
                Object[] array = userPrompt.toArray();
                list.add(arguments(model, array[0], array[1]));
            }
        }
        return list.stream();
    }


}
