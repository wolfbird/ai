package com.econage.ai.support.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;

interface CacheBase<KY, VL> {

    /*
     * 通过id数组查询值列表
     * */
    default List<VL> getList(KY... ids){
        if(ArrayUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return getList(Arrays.asList(ids));
    }

    /*
     * 通过id集合查询值列表
     * */
    default List<VL> getList(Collection<KY> ids){
        if(CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        return ids.stream()
                .map(this::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /*
     * 通过键值查询值
     * */
    VL get(KY key);

}
