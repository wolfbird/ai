package com.econage.ai.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author tzl
 */
/*
 * e10服务配置
 * */
@Data
@ConfigurationProperties(EconageConst.NAME_SPACE)
public class EconageSetting {

    private String baseUrl;

}
