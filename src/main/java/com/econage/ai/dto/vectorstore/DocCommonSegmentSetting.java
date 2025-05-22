package com.econage.ai.dto.vectorstore;

import lombok.Data;

/**
 * 普通分段设置
 * @author wy
 * @date 2025/3/3
 */
@Data
public class DocCommonSegmentSetting {

    /**
     * 分段标识符
     */
    private String segmentIdentifier;

    /**
     * 分段最大长度
     */
    private Integer segmentMaxLength;

    /**
     * 分段重叠度
     */
    private Integer segmentOverlap;

}
