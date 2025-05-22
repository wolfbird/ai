package com.econage.ai.support.cache;

/*
* 允许像map结构一样写入具体值信息，并按照最大数、失效时间淘汰数据
* 次数据结构非常依赖底层集群组件，简化逻辑，key值固定为String类型
* */
public interface PlatMapCache<VL> extends CacheBase<String, VL> {

    /*
    * 设置具体值
    * */
    void put(String key,VL val);

    /*
     * 作废某个key对应的缓存
     * */
    void invalidData(String key);

}
