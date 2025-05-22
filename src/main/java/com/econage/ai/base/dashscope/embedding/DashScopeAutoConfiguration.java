package com.econage.ai.base.dashscope.embedding;

import com.econage.ai.base.config.DefaultConnectionProperties;
import com.econage.ai.base.config.DefaultEmbeddingProperties;
import com.econage.ai.base.config.ParentProperties;
import com.econage.ai.base.dashscope.api.DashScopeApi;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author hanpeng
 * @date 2025/2/18 11:09
 */
@AutoConfiguration
@EnableConfigurationProperties({ DefaultConnectionProperties.class, DefaultEmbeddingProperties.class })
public class DashScopeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DashScopeEmbeddingModel dashscopeEmbeddingModel(
            DefaultConnectionProperties commonProperties,
            DefaultEmbeddingProperties embeddingProperties,
            RestClient.Builder restClientBuilder,
            WebClient.Builder webClientBuilder,
            RetryTemplate retryTemplate,
            ResponseErrorHandler responseErrorHandler
    ) {

        var dashScopeApi = dashscopeEmbeddingApi(commonProperties, embeddingProperties, restClientBuilder,
                webClientBuilder, responseErrorHandler);

        return new DashScopeEmbeddingModel(dashScopeApi, embeddingProperties.getMetadataMode(),
                embeddingProperties.getOptions(), retryTemplate);
    }


    public DashScopeApi dashscopeEmbeddingApi(
            DefaultConnectionProperties commonProperties,
            DefaultEmbeddingProperties embeddingProperties,
            RestClient.Builder restClientBuilder,
            WebClient.Builder webClientBuilder,
            ResponseErrorHandler responseErrorHandler
    ) {

        ResolvedConnectionProperties resolved = resolveConnectionProperties(commonProperties,
                embeddingProperties, "embedding");

        return new DashScopeApi(resolved.baseUrl(), resolved.apiKey(), restClientBuilder,
                webClientBuilder, responseErrorHandler);
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

    private record ResolvedConnectionProperties(String baseUrl, String apiKey, MultiValueMap<String, String> headers) {
    }
}
