package com.hawolt.client.cache;

/**
 * Created: 24/09/2023 02:52
 * Author: Twitter @hawolt
 **/

public interface CacheListener<T> {
    void onCacheUpdate(T value);
}
