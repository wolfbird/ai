package com.econage.ai;

import com.econage.ai.support.factory.ChatModelFactory;
import com.econage.ai.support.meta.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
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
class IntentTest {
    @Autowired
    private ChatModelFactory chatModelFactory;

    @Test
    void testWithTool() {
        ChatModel chatModel = chatModelFactory.createChatModel(ProviderType.DASH_SCOPE, "qwen-turbo",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-3df07a6905a04c2f9389569dc9790e95");
        log.info("response {}", execute( chatModel , "我想要查询账户余额"));
        log.info("response {}", execute( chatModel , "我想要查看账号列表"));
        log.info("response {}", execute( chatModel , "我想查看用户信息"));
        log.info("response {}", execute( chatModel , "我想把账号删了"));
        log.info("response {}", execute( chatModel , "删除账号"));
        log.info("response {}", execute( chatModel , "删除账户"));
    }

    @Nullable
    private static String execute(ChatModel chatModel , String userMessage) {
        return ChatClient.create(chatModel)
                .prompt(new Prompt(
                        new SystemMessage(
                        """
                               您是一个智能助手，负责根据用户的输入内容，识别最匹配的方法 ID。
                               ## 方法列表
                               请参考以下方法列表：
                               - 方法 ID: `method_001`，描述: `创建用户账户` \s
                               - 方法 ID: `method_002`，描述: `重置用户密码` \s
                               - 方法 ID: `method_003`，描述: `查询账户余额` \s
                               - 方法 ID: `method_004`，描述: `更新用户信息` \s
                               - 方法 ID: `method_005`，描述: `删除用户账户` \s
                               ##  输出要求
                               - 如果用户输入与某个方法描述高度相关，请返回对应的 **方法 ID**
                               - 如果没有匹配的项，请返回 NONE
                               - 如果不确定、不明确、或有歧义，请务必返回 NONE
                               - 请不要根据猜测或模糊理解选择方法 ID
                               ##  示例
                               - 用户输入: `我想注册一个新账户` \s
                                 输出: method_001
                               - 用户输入: `请帮我修改密码` \s
                                 输出: method_002
                               - 用户输入: `天气怎么样？` \s
                                 输出: NONE
                                """),
                        new UserMessage(userMessage)
                ) )
                .call()
                .content();
    }


}
