package com.econage.ai.support.cache;

/*
* 平台变频缓存，缓存对象之间无交集，刷新动作由自身负责执行
* */
public interface PlatPlainCache<KY, VL> extends CacheBase<KY, VL>  {

    /*
     * 作废某个key对应的缓存
     * */
    void invalidData(KY key);

    /*
    * 作废所有键对应的缓存
    * */
    void invalidAllData();

}
