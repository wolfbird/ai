package com.econage.ai.support.factory.provider;

import com.econage.ai.base.config.ChatProperties;
import com.econage.ai.base.config.ConnectionProperties;
import com.econage.ai.base.config.ParentProperties;
import com.econage.ai.base.openai.OpenAiChatModel;
import com.econage.ai.base.openai.OpenAiChatOptions;
import com.econage.ai.base.openai.api.OpenAiApi;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.support.meta.ProviderType;
import io.micrometer.observation.ObservationRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.autoconfigure.openai.OpenAiEmbeddingProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hanpeng
 * @date 2025/2/8 11:08
 */
@Component
public class OpenAIChatModelProvider implements ChatModelProvider {

    private RestClient.Builder restClientBuilder;
    private WebClient.Builder webClientBuilder;
    private RetryTemplate retryTemplate;
    private ResponseErrorHandler responseErrorHandler;
    private ObjectProvider<ObservationRegistry> observationRegistry;
    private ObjectProvider<ChatModelObservationConvention> observationConvention;
    private static final String DEFAULT_COMPLETIONS_PATH = "/chat/completions";

    @Autowired
    void auto(
            RestClient.Builder restClientBuilder,
            WebClient.Builder webClientBuilder,
            RetryTemplate retryTemplate,
            ResponseErrorHandler responseErrorHandler,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<ChatModelObservationConvention> observationConvention
    ) {
        this.restClientBuilder = restClientBuilder;
        this.webClientBuilder = webClientBuilder;
        this.retryTemplate = retryTemplate;
        this.responseErrorHandler = responseErrorHandler;
        this.observationRegistry = observationRegistry;
        this.observationConvention = observationConvention;
    }



    private static final List<ProviderType> SUPPORTED_PROVIDER = List.of(ProviderType.OLLAMA,ProviderType.DEEP_SEEK,ProviderType.OPENAI_COMPATIBLE);


    @Override
    public ChatModel createChatModel( ProviderType providerType, String model, ConnectionProperties commonProperties, String apiKey) {
        ChatProperties<OpenAiChatOptions> chatProperties = new ChatProperties<>();
        chatProperties.setApiKey(apiKey);

        var options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(0.0d)
                .build();

        if (StringUtils.isEmpty(commonProperties.getBaseUrl())) {
            commonProperties.setBaseUrl(providerType.getDefaultBaseUrl());
        }

        chatProperties.setOptions(options);

        var openAiApi = openAiApi(commonProperties, chatProperties , restClientBuilder, webClientBuilder,
                responseErrorHandler, "chat");

        var chatModel = new OpenAiChatModel(openAiApi, chatProperties.getOptions());

        observationConvention.ifAvailable(chatModel::setObservationConvention);

        return chatModel;
    }

    @Override
    public ChatOptions createChatOptions(ChatCompletionRequest chatRequest) {
        return null;
    }

    private OpenAiApi openAiApi(ConnectionProperties chatProperties, ChatProperties<OpenAiChatOptions> commonProperties,
                                RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder,
                                ResponseErrorHandler responseErrorHandler, String modelType) {

        ResolvedConnectionProperties resolved = resolveConnectionProperties(commonProperties, chatProperties,
                modelType);

        return new OpenAiApi(resolved.baseUrl(), resolved.apiKey(), resolved.headers(),
                DEFAULT_COMPLETIONS_PATH, OpenAiEmbeddingProperties.DEFAULT_EMBEDDINGS_PATH,
                restClientBuilder, webClientBuilder, responseErrorHandler);
    }


    private static ResolvedConnectionProperties resolveConnectionProperties(
            ParentProperties commonProperties, ParentProperties modelProperties, String modelType) {

        String baseUrl = StringUtils.isNotEmpty(modelProperties.getBaseUrl()) ? modelProperties.getBaseUrl()
                : commonProperties.getBaseUrl();
        String apiKey = StringUtils.isNotEmpty(modelProperties.getApiKey()) ? modelProperties.getApiKey()
                : commonProperties.getApiKey();
        String projectId = StringUtils.isNotEmpty(modelProperties.getProjectId()) ? modelProperties.getProjectId()
                : commonProperties.getProjectId();
        String organizationId = StringUtils.isNotEmpty(modelProperties.getOrganizationId())
                ? modelProperties.getOrganizationId() : commonProperties.getOrganizationId();

        Map<String, List<String>> connectionHeaders = new HashMap<>();
        if (StringUtils.isNotEmpty(projectId)) {
            connectionHeaders.put("OpenAI-Project", List.of(projectId));
        }
        if (StringUtils.isNotEmpty(organizationId)) {
            connectionHeaders.put("OpenAI-Organization", List.of(organizationId));
        }

        Assert.hasText(baseUrl,
                "OpenAI base URL must be set.  Use the connection property: spring.ai.openai.base-url or spring.ai.openai."
                        + modelType + ".base-url property.");

        return new ResolvedConnectionProperties(baseUrl, apiKey, CollectionUtils.toMultiValueMap(connectionHeaders));
    }

    private record ResolvedConnectionProperties(String baseUrl, String apiKey, MultiValueMap<String, String> headers) {
    }


    @Override
    public boolean support(ProviderType providerType) {
        return true;
    }
}
