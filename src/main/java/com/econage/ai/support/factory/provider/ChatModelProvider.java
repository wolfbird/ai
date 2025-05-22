package com.econage.ai.support.factory.provider;

import com.econage.ai.base.config.ConnectionProperties;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.support.meta.ProviderType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;

/**
 * @author hanpeng
 * @date 2025/2/8 11:06
 */
public interface ChatModelProvider {

    /**
     * 生成模型
      * @return
     */
    ChatModel createChatModel( ProviderType providerType, String model , ConnectionProperties commonProperties, String apiKey );

    /**
     * 生成对话配置
     * @return
     */
    ChatOptions createChatOptions(ChatCompletionRequest chatRequest );


    /**
     * 是否支持对应供应商
     * @param providerType
     * @return
     */
    boolean support( ProviderType providerType);
}
