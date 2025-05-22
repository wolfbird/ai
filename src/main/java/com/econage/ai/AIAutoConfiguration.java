package com.econage.ai;

import com.econage.ai.boot.mvc.response.WebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * @author hanpeng
 * @date 2024/9/24 15:21
 */
@ComponentScan(basePackageClasses = {AIPackageInfo.class})
@Configuration
public class AIAutoConfiguration {

    @Bean
    public WebExceptionHandler webGlobalExceptionHandler(){
        return new WebExceptionHandler();
    }



}
