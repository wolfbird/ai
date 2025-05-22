package com.econage.ai.support.cache.impl.plain;


import lombok.NonNull;

public class LocalPlainCacheCoordinator extends PlainCacheCoordinator {

    public LocalPlainCacheCoordinator(@NonNull PlainCacheImpl<?,?> linkedCacheImpl) {
        super(linkedCacheImpl);
    }

    @Override
    protected void invalidData(Object key) {
        invalidDataLocal(key);
    }

    @Override
    protected void invalidAllData() {
        invalidAllDataLocal();
    }

}
