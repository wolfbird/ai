package com.econage.ai.support.meta;

/**
 * @author hanpeng
 * @date 2025/2/8 11:12
 */
public enum ProviderType {
    DEEP_SEEK("https://api.deepseek.com"),
    DASH_SCOPE( "https://dashscope.aliyuncs.com/compatible-mode/v1" ),
    OLLAMA(""),
    DOUBAO(""),
    KIMI(""),
    MOONSHOT(""),
    BAICHUAN(""),
    XUNFEI(""),
    ZHIPU(""),
    HUAWEI(""),
    WENXIN(""),
    GOOGLE(""),
    MINMAX(""),
    AZURE_OPENAI(""),
    OPENAI(""),
    OPENAI_COMPATIBLE("");


    private final String defaultBaseUrl;

    ProviderType ( String defaultBaseUrl ){
        this.defaultBaseUrl = defaultBaseUrl;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }
}
