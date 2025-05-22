package com.econage.ai.base.dashscope.common;

import com.econage.ai.base.dashscope.observation.conventions.AiProvider;

/**
 * @author hanpeng
 * @date 2025/2/8 10:32
 */
public final class DashScopeApiConstants {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    public static final String HEADER_OPENAPI_SOURCE = "X-DashScope-OpenAPISource";

    public static final String HEADER_WORK_SPACE_ID = "X-DashScope-Workspace";

    public static final String SOURCE_FLAG = "CloudSDK";

    public static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com";

    public static final String DASHSCOPE_API_KEY = "DASHSCOPE_API_KEY";

    public static final Integer DEFAULT_READ_TIMEOUT = 60;

    public static final String PROVIDER_NAME = AiProvider.DASHSCOPE.value();

}