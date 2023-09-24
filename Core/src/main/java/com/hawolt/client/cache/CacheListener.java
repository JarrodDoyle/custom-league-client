package com.hawolt.client.cache;

/**
 * Created: 24/09/2023 10:39
 * Author: Twitter @hawolt
 **/

public interface CacheListener<T> {
    void onCacheUpdate(CacheElement element, T o);
}
