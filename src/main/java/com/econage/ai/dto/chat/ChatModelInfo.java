package com.econage.ai.dto.chat;

import com.econage.ai.support.meta.ProviderType;
import lombok.Data;

import java.util.Objects;

/**
 * @author hanpeng
 * @date 2025/2/7 17:29
 */
@Data
public class ChatModelInfo {

    /**
     * 供应商名称
     */
    private ProviderType providerType;
    /**
     * 模型名称
     */
    private String model;

    /**
     * apiKey
     */
    private String apiKey;

    /**
     * 基础 URL
     */
    private String baseUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatModelInfo that = (ChatModelInfo) o;
        return providerType == that.providerType && Objects.equals(model, that.model) && Objects.equals(apiKey, that.apiKey) && Objects.equals(baseUrl, that.baseUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerType, model, apiKey, baseUrl);
    }
}
