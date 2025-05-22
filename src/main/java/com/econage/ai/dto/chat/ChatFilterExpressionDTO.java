package com.econage.ai.dto.chat;

import lombok.Data;
import com.econage.ai.base.vectorstore.filter.Filter;

/**
 * @author hanpeng
 * @date 2025/2/19 15:35
 */
@Data
public class ChatFilterExpressionDTO {

    private String key;

    private Object value;

    private Filter.ExpressionType expressionType;
}
