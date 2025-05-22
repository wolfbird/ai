package com.econage.ai.support.cache.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data
@Builder
public class PlainCacheEntrySetting<K,V> {

    /*
    * 缓存名称
    * */
    private String name;

    /*
    * 缓存最大容量
    * */
    private int maximumSize;

    /*
    * 返回的缓存对象，是否强制执行克隆操作
    * */
    private boolean clone;

    /*
    * 键类型
    * */
    private Class<K> keyClazz;

    /*
    * entity类型
    * */
    private Class<? super V> valueClazz;

    /*
     * 是否永久保持，true时，忽略expireTimeAfterWrite
     * */
    private boolean eternal;

    /*
     * 单挑缓存数据过期时间
     * */
    private Duration expireTimeAfterWrite;

}
