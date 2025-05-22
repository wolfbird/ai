package com.econage.ai.dto.vectorstore;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 向量信息校验权限request
 *
 * @author tangzhanglin
 * @date 2025/4/08 15:30
 */
@Data
public class VectorPermissionCheckRequest implements Serializable {

    /**
     * 需要校验权限的向量信息
     */
    private List<VectorPermissionCheckUnion> unions;


    @Data
    public static class VectorPermissionCheckUnion implements Serializable {

        /**
         * 数据来源
         */
        private String fileSourceType;

        /**
         * 模块ids
         */
        private List<String> modularInnerIds;
    }
}
