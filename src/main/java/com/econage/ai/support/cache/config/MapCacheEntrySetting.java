package com.econage.ai.support.cache.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data
@Builder
public class MapCacheEntrySetting {

    private String name;

    private int maxSize;

    /*
     * 是否永久保持，true时，忽略expireTimeAfterWrite
     * */
    private boolean eternal;

    private Duration ttl;

    private boolean isClone;

    private Class<?> valueClazz;

}
