package com.econage.ai.dto.chat;

import lombok.Data;

import java.util.List;

/**
 * @author hanpeng
 * @date 2025/1/7 14:36
 */
@Data
public class ChatCompletionRequest {

    /**
     * 模型相关信息
     */
    private ChatModelInfo chatModelInfo;

    /**
     * 历史对话信息
     * todo 后续融合后应该直接从持久化记录中获取
     */
    private List<ChatMemoryDTO> messagesMemory;

    /**
     * 系统设定
     */
    private String systemText;

    /**
     * 输入信息
     */
    private String queryMessage;

    /**
     * 会话id
     */
    private String conversationId;

    /**
     * 单次对话id
     */
    private String messageId;

    /**
     * 最大 token 上限
     */
    private Long maxTokens;

    /**
     * 是否引用向量数据库
     */
    private boolean withVectorStore;
    /**
     * 向量匹配得分阈值
     */
    private Double similarityThreshold;

    /**
     * 向量搜索结果个数
     */
    private Integer topK;

    /**
     * 向量过滤条件
     */
    private List<ChatFilterExpressionDTO> filterExpression;

    /**
     * 多模态媒体类型
     */
    private List<ChatMediaDTO> mediaList;


    /**
     * 文件解析文本
     */
    private List<ChatFileDTO> fileTextList;
}
