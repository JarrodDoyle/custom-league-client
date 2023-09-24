package com.hawolt.client.cache;

/**
 * Created: 14/08/2023 19:42
 * Author: Twitter @hawolt
 **/

public interface ISimpleValueCache<T, S> {

    void register(CacheElement element, CacheListener<?> listener);

    void cache(T type, ExceptionalSupplier<S> o);

    void dispatch(CacheElement element);

    void cache(T type, S o);
}
