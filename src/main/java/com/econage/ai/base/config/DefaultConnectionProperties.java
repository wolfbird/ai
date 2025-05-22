package com.econage.ai.base.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import static com.econage.ai.base.dashscope.common.DashScopeApiConstants.DEFAULT_BASE_URL;
import static com.econage.ai.base.dashscope.common.DashScopeApiConstants.DEFAULT_READ_TIMEOUT;

/**
 * @author hanpeng
 * @date 2025/2/17 19:29
 */
@ConfigurationProperties(DefaultConnectionProperties.CONFIG_PREFIX)
public class DefaultConnectionProperties extends ParentProperties{

    public static final String CONFIG_PREFIX = "spring.ai.default";

    private Integer readTimeout;

    public DefaultConnectionProperties() {
        if( !StringUtils.hasText(super.getBaseUrl())) {
            super.setBaseUrl(DEFAULT_BASE_URL);
        }
        readTimeout = DEFAULT_READ_TIMEOUT;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }
}
