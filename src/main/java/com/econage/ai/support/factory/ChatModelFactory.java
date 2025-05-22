package com.econage.ai.support.factory;

import com.econage.ai.base.config.ConnectionProperties;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.support.meta.ProviderType;
import com.econage.ai.support.factory.provider.ChatModelProvider;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author hanpeng
 * @date 2025/2/8 11:01
 */
@Component
public class ChatModelFactory {

    private Collection<ChatModelProvider> providers;


    @Autowired
    void auto(Collection<ChatModelProvider> providers) {
        this.providers = providers;
    }

    public ChatModel createChatModel( ProviderType providerType, String model ,String baseUrl, String apiKey) {

        var provider = providers.stream()
                .filter(p -> p.support(providerType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("not support provider type: " + providerType));

        var commonProperties = new ConnectionProperties();
        commonProperties.setBaseUrl(baseUrl);
        

        return provider.createChatModel( providerType, model, commonProperties, apiKey);
    }

    public ChatOptions createChatOptions( ProviderType providerType, ChatCompletionRequest chatRequest) {
        var provider = providers.stream()
                .filter(p -> p.support(providerType))
                .findFirst()
                .orElse(null);

        if(provider == null) {
            return null;
        }

        return provider.createChatOptions( chatRequest );

    }
}
