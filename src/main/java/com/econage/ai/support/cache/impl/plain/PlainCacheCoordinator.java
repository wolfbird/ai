package com.econage.ai.support.cache.impl.plain;

import lombok.NonNull;

public abstract class PlainCacheCoordinator {

    private final PlainCacheImpl<?,?> linkedCacheImpl;

    protected PlainCacheCoordinator(@NonNull PlainCacheImpl<?,?> linkedCacheImpl) {
        this.linkedCacheImpl = linkedCacheImpl;
    }

    /*
     * 发送键值失效信号
     * */
    protected abstract void invalidData(Object key);

    /*
     * 发送新数据版本信号
     * */
    protected abstract void invalidAllData();

    /*
     * 作废本地缓存某个键值
     * */
    protected final void invalidDataLocal(Object key){
        linkedCacheImpl.invalidDataLocal(key);
    }

    /*
     * 作废本地缓存全部键值
     * */
    protected final void invalidAllDataLocal(){
        linkedCacheImpl.invalidAllDataLocal();
    }

}
