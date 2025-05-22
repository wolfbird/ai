package com.econage.ai.dto.vectorstore;

import com.econage.ai.support.meta.DocSegmentType;
import lombok.Data;

/**
 * @author wy
 * @date 2025/3/3
 */
@Data
public class DocSegmentSettingUnion {

    /**
     * 分段类型
     */
    private DocSegmentType segmentType;

    /**
     * 普通分段
     */
    private DocCommonSegmentSetting commonSegmentSetting;


}
