package com.econage.ai;

import com.econage.ai.support.factory.ChatModelFactory;
import com.econage.ai.support.meta.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

/**
 * @author tzl
 */
@Slf4j
@SpringBootTest(classes = {AiDemoApplication.class})
class ToolCallingTest {
    @Autowired
    private ChatModelFactory chatModelFactory;

    @Test
    void testWithTool() {
        ChatModel chatModel = chatModelFactory.createChatModel(ProviderType.DASH_SCOPE, "qwen-turbo",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-3df07a6905a04c2f9389569dc9790e95");
        String response = ChatClient.create(chatModel)
                .prompt("我的所在地2025年6月30号天气如何")
                .tools(new WeatherTools(),  new LocationTools())
                .call()
                .content();
        log.info("response {}", response);
        Assertions.assertNotNull(response, "异常返回");
        Assertions.assertTrue(response.contains(Runtime.version().toString()), "异常返回");
    }

    @Test
    void testNoTool() {
        ChatModel chatModel = chatModelFactory.createChatModel(ProviderType.DASH_SCOPE, "qwen-turbo",
                "https://dashscope.aliyuncs.com", "sk-3df07a6905a04c2f9389569dc9790e95");
        String response = ChatClient.create(chatModel)
                .prompt("程序用的是什么版本的java")
                .call()
                .content();
        log.info("response {}", response);
        Assertions.assertNotNull(response, "异常返回");
        Assertions.assertFalse(response.contains(Runtime.version().toString()), "异常返回");
    }


    static class WeatherTools {
        @Tool(description = "获取指定地区和时间的天气")
        String getWeather(
                @ToolParam(description = "地点") String location,
                @ToolParam(description = "当前时间") LocalDateTime dateTime
        ) {
            return "晴天";
        }

    }

//    static class TimeTools {
//        @Tool(description = "获取当前时间")
//        String getCurrentTime() {
//            return LocalDateTime.now().toString();
//        }
//
//    }

    static class LocationTools {
        @Tool(description = "获取当前地区位置，当不明确用户所在地点时，可使用该方法获取用户所在地点")
        String getLocation(
        ) {
            return "杭州";
        }

    }

}
