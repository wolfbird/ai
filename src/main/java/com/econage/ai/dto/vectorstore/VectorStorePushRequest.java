package com.econage.ai.dto.vectorstore;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author hanpeng
 * @date 2025/2/18 14:50
 */
@Data
public class VectorStorePushRequest {

    /**
     * 同步单元
     */
    private List<VectorStorePushUnion> unions;



    @Data
    public static class VectorStorePushUnion {

        /**
         * 关联标识
         * 物理文件： fileHeaderId值
         * 知识库文件： 知识库文件主键id
         */
        private String modularInnerId;

        /**
         * 解析出的文件内容
         */
        private String docText;

        /**
         * 元数据信息
         * 应当包括 source：文件名称
         *        docDatasource：文件来源
         */
        private Map<String, Object> metaDataMap;


        /**
         * 知识文档分段设置
         */
        private DocSegmentSettingUnion segmentSettingUnion;
    }
}
