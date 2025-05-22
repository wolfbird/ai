package com.econage.ai.support.cache;

import com.econage.ai.support.cache.config.MapCacheEntrySetting;
import com.econage.ai.support.cache.config.PlainCacheEntrySetting;
import com.econage.ai.support.cache.impl.map.LocalMapCache;
import com.econage.ai.support.cache.impl.plain.LocalPlainCacheCoordinator;
import com.econage.ai.support.cache.impl.plain.PlainCacheCoordinator;
import com.econage.ai.support.cache.impl.plain.PlainCacheImpl;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.function.Function;

/**
 * @author hanpeng
 * @date 2025/1/9 11:37
 */
@Component
public class CacheManager {
    private ThreadPoolTaskExecutor executor;

    @Autowired
    void setWired(
            ThreadPoolTaskExecutor threadPoolTaskExecutor
    ) {
        this.executor = threadPoolTaskExecutor;
    }

    /*
     * 创建根据最大数、失效时间淘汰值数据的分布式map
     * */
    public <VL> PlatMapCache<VL> createPlatMapCache(@NonNull MapCacheEntrySetting mapCacheEntrySetting){
        Assert.hasText(mapCacheEntrySetting.getName(),"name is empty");
        Assert.isTrue(mapCacheEntrySetting.getMaxSize()>0,"maxSize is negative");
        Assert.notNull(mapCacheEntrySetting.getTtl(),"ttl is empty");
        return new LocalMapCache<>(mapCacheEntrySetting,executor);
    }

    /*
     * 请确保key值可以被json类库正确的序列化反序列化，推荐使用String类型
     * */
    public <KY, VL> PlatPlainCache<KY, VL> createPlainCache(
            @NonNull final PlainCacheEntrySetting<KY, VL> plainCacheEntrySetting,
            @NonNull final Function<KY, VL> cacheLoader
    ){
        Assert.hasText(plainCacheEntrySetting.getName(),"name is empty");
        return new PlainCacheImpl<>(
                plainCacheEntrySetting,
                cacheLoader,
                factoryFunction(),
                executor
        );
    }

    private <KE, VL> Function<PlainCacheImpl<KE, VL>, PlainCacheCoordinator> factoryFunction(){
        return LocalPlainCacheCoordinator::new;
    }
}
