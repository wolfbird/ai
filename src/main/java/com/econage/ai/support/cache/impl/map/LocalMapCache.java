package com.econage.ai.support.cache.impl.map;


import com.econage.ai.support.cache.PlatMapCache;
import com.econage.ai.support.cache.config.MapCacheEntrySetting;
import lombok.Getter;
import lombok.val;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.expiry.Expiry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class LocalMapCache<VL> implements PlatMapCache<VL> {

    @Getter
    private final String name;
    private final Cache<String, Object> localCache;

    public LocalMapCache(
            MapCacheEntrySetting mapCacheEntrySetting,
            ThreadPoolTaskExecutor executor
    ){
        this.name = mapCacheEntrySetting.getName();

        Cache2kBuilder<String, Object> cacheBuilder = Cache2kBuilder
                .forUnknownTypes()
                .permitNullValues(true)
                .entryCapacity(mapCacheEntrySetting.getMaxSize())
                .executor(executor)
                .keyType(String.class);

        if(mapCacheEntrySetting.isEternal()){
            cacheBuilder.eternal(true);
        }else{
            cacheBuilder.expireAfterWrite(mapCacheEntrySetting.getTtl());
        }

        this.localCache = cacheBuilder.build();
    }

    @Override
    public VL get(String key) {
        if(key==null){
            return null;
        }
        try {
            val entity =localCache.get(key);

            return (VL)entity;
        }catch(Exception e){
            throw e;
        }
    }

    @Override
    public void put(String key, VL val) {
        if(val==null){
            return;
        }
        localCache.put(key,val);
    }

    @Override
    public void invalidData(String key) {
        localCache.expireAt(key,Expiry.NOW);
    }

}
