package com.econage.ai.support.factory.provider;

import com.econage.ai.base.config.ChatProperties;
import com.econage.ai.base.config.ConnectionProperties;
import com.econage.ai.base.config.ParentProperties;
import com.econage.ai.base.dashscope.api.DashScopeApi;
import com.econage.ai.base.dashscope.chat.DashScopeChatModel;
import com.econage.ai.base.dashscope.chat.DashScopeChatOptions;
import com.econage.ai.base.dashscope.common.DashScopeApiConstants;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.support.meta.ProviderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * todo 验证可以用openai完全兼容dashscope后，废弃掉
 * @author hanpeng
 * @date 2025/2/8 11:08
 */
@Component
public class DashScopeChatModelProvider implements ChatModelProvider {

    private RestClient.Builder restClientBuilder;
    private WebClient.Builder webClientBuilder;
    private ResponseErrorHandler responseErrorHandler;

    @Autowired
    void auto(
            RestClient.Builder restClientBuilder,
            WebClient.Builder webClientBuilder,
            ResponseErrorHandler responseErrorHandler
    ) {
        this.restClientBuilder = restClientBuilder;
        this.webClientBuilder = webClientBuilder;
        this.responseErrorHandler = responseErrorHandler;
    }



    private static final List<ProviderType> SUPPORTED_PROVIDER = List.of(ProviderType.DASH_SCOPE);


    @Override
    public ChatModel createChatModel(ProviderType providerType, String model, ConnectionProperties commonProperties, String apiKey) {
        ChatProperties<DashScopeChatOptions> chatProperties = new ChatProperties<>();
        chatProperties.setApiKey(apiKey);

        var options = DashScopeChatOptions.builder()
                .withModel(model)
                .withTemperature(0.0d)
                .withEnableSearch(true)
                .build();

        if (StringUtils.isEmpty(commonProperties.getBaseUrl())) {
            commonProperties.setBaseUrl(DashScopeApiConstants.DEFAULT_BASE_URL);
        }

        chatProperties.setOptions(options);

        var dashscopeApi = dashscopeChatApi(commonProperties, chatProperties, restClientBuilder, webClientBuilder,
                responseErrorHandler);

        return new DashScopeChatModel(dashscopeApi, chatProperties.getOptions());
    }

    @Override
    public ChatOptions createChatOptions(ChatCompletionRequest chatRequest) {

        return DashScopeChatOptions.builder()
                .withMultiModel(CollectionUtils.isNotEmpty(chatRequest.getMediaList()))
                .build();
    }


    public DashScopeApi dashscopeChatApi(
            ConnectionProperties commonProperties,
            ChatProperties chatProperties,
            RestClient.Builder restClientBuilder,
            WebClient.Builder webClientBuilder,
            ResponseErrorHandler responseErrorHandler
    ) {

        ResolvedConnectionProperties resolved = resolveConnectionProperties(commonProperties, chatProperties, "chat");

        return new DashScopeApi(resolved.baseUrl, resolved.apiKey, null, restClientBuilder,
                webClientBuilder, responseErrorHandler);
    }

    private record ResolvedConnectionProperties(String baseUrl, String apiKey, String workspaceId) {
    }

    private static ResolvedConnectionProperties resolveConnectionProperties(
            ParentProperties commonProperties,
            ParentProperties modelProperties,
            String modelType
    ) {

        String baseUrl = StringUtils.isNotEmpty(modelProperties.getBaseUrl()) ? modelProperties.getBaseUrl()
                : commonProperties.getBaseUrl();
        String apiKey = StringUtils.isNotEmpty(modelProperties.getApiKey()) ? modelProperties.getApiKey()
                : commonProperties.getApiKey();


        Assert.hasText(baseUrl,
                "DashScope base URL must be set.  Use the connection property: spring.ai.dashscope.base-url or spring.ai.dashscope."
                        + modelType + ".base-url property.");
        Assert.hasText(apiKey,
                "DashScope API key must be set. Use the connection property: spring.ai.dashscope.api-key or spring.ai.dashscope."
                        + modelType + ".api-key property.");

        return new ResolvedConnectionProperties(baseUrl, apiKey, null);
    }


    @Override
    public boolean support(ProviderType providerType) {
        return false;
    }
}
