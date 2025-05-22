package com.econage.ai.support.cache.impl.plain;

import com.econage.ai.support.cache.PlatPlainCache;
import com.econage.ai.support.cache.config.PlainCacheEntrySetting;
import lombok.Getter;
import lombok.val;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.expiry.Expiry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

import java.util.function.Function;

public class PlainCacheImpl<K,T> implements PlatPlainCache<K,T> {

    @Getter
    private final String name;
    private final Cache<Object,Object> localCache;
    /*
    * 键类型，用于集群环境执行反序列化
    * */
    @Getter
    private final Class<K> keyClazz;
    private final PlainCacheCoordinator plainCacheCoordinator;

    public PlainCacheImpl(
            PlainCacheEntrySetting<K,T> plainCacheEntrySetting,
            Function<K, T> rawCacheLoader,
            Function<PlainCacheImpl<K,T>,PlainCacheCoordinator> factory,
            ThreadPoolTaskExecutor executor
    ){
        this.name = plainCacheEntrySetting.getName();
        this.keyClazz = plainCacheEntrySetting.getKeyClazz();
        Assert.notNull(keyClazz,"keyClz is empty");

        Cache2kBuilder<Object, Object> cacheBuilder = Cache2kBuilder
                .forUnknownTypes()
                .permitNullValues(true)
                .entryCapacity(plainCacheEntrySetting.getMaximumSize())
                .loader( rawKey-> rawCacheLoader.apply((K)rawKey) )
                .executor(executor);

        if(plainCacheEntrySetting.isEternal()){
            cacheBuilder.eternal(true);
        }else{
            cacheBuilder.expireAfterWrite(plainCacheEntrySetting.getExpireTimeAfterWrite());
        }

        cacheBuilder.keyType(keyClazz);
        if(plainCacheEntrySetting.getValueClazz()!=null){
            Class<?> entityClass = plainCacheEntrySetting.getValueClazz();
            cacheBuilder.valueType(entityClass);
        }

        this.localCache = cacheBuilder.build();
        this.plainCacheCoordinator = factory.apply(this);
    }

    @Override
    public T get(K key) {
        if(key==null){
            return null;
        }
        try {
            val entity =localCache.get(key);
            return (T)entity;
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e) {
            throw e;
        }
    }

    @Override
    public void invalidData(K key) {
        plainCacheCoordinator.invalidData(key);
    }

    @Override
    public void invalidAllData() {
        plainCacheCoordinator.invalidAllData();
    }

    /*
    * 信息传输过程中的键值，会尝试做反序列化
    * */
    void invalidDataLocal(Object key){
        localCache.expireAt(key, Expiry.NOW);
    }

    /*
    * 作废全部本地缓存
    * */
    void invalidAllDataLocal(){
        localCache.clear();
    }

}
