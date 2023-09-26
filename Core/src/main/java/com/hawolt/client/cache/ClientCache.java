package com.hawolt.client.cache;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.exceptional.ExceptionalFunction;
import com.hawolt.client.exceptional.ExceptionalSupplier;
import com.hawolt.generic.data.Unsafe;
import com.hawolt.logger.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created: 24/09/2023 09:32
 * Author: Twitter @hawolt
 **/

public abstract class ClientCache implements ISimpleValueCache<CacheElement, Object>, Consumer<CachedValueLoader<CacheElement, ?>> {
    private final Map<CacheElement, ExceptionalFunction<LeagueClient, ?>> sources = new HashMap<>();

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final Map<CacheElement, List<CacheListener<?>>> listeners = new HashMap<>();
    private final Map<CacheElement, Object> cache = new HashMap<>();

    public void setElementSource(CacheElement element, ExceptionalFunction<LeagueClient, ?> function) {
        this.sources.put(element, function);
    }

    @Override
    public void register(CacheElement element, CacheListener<?> listener) {
        Logger.debug("Register {} listener, source: {}", element, listener.getClass().getCanonicalName());
        if (!listeners.containsKey(element)) listeners.put(element, new LinkedList<>());
        listeners.get(element).add(listener);
    }

    @Override
    public void cache(CacheElement type, ExceptionalSupplier<Object> o) {
        try {
            cache(type, o.get());
            dispatch(type);
        } catch (Exception e) {
            Logger.error("Failed to fetch cache value for {}", type);
        }
    }

    @Override
    public void dispatch(CacheElement element) {
        Logger.debug("dispatch {} from cache", element);
        if (!listeners.containsKey(element)) return;
        Object reference = cache.get(element);
        this.listeners.get(element).forEach(listener -> listener.onCacheUpdate(element, Unsafe.cast(reference)));
    }

    @Override
    public void cache(CacheElement element, Object value) {
        Logger.info("Storing value for {} in cache as {}", element, value);
        if (element.getCachedDataType() != CachedDataType.JWT) {
            this.cache.put(element, value);
            this.dispatch(element);
        } else {
            JWT jwt = new JWT(value.toString());
            if (jwt.isExpired()) {
                Logger.warn("Attempting to store expired JWT in Cache has been prevented");
            } else {
                this.cache.put(element, jwt);
                this.dispatch(element);
                long timestamp = Math.max(
                        1,
                        jwt.getExpirationTime() - System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)
                );
                this.service.schedule(
                        () -> {
                            Logger.info("Removing {} from cache, JWT expired", element);
                            cache.remove(element);
                        },
                        timestamp,
                        TimeUnit.MILLISECONDS
                );
            }
        }
    }

    public <T> T getCachedValue(CacheElement element) {
        if (!cache.containsKey(element)) {
            Logger.info("Fetch cache value for {}", element);
            try {
                Object value = sources.get(element).apply(getClient());
                cache(element, value);
            } catch (Exception e) {
                Logger.error("Failed to fetch cache value for {}", element);
            }
        }
        if (!cache.containsKey(element)) throw new CacheException(element);
        Logger.info("Get cache value for {}", element);
        return Unsafe.cast(cache.get(element));
    }

    public <T> T getCachedValueOrElse(CacheElement type, ExceptionalSupplier<T> supplier) throws Exception {
        if (cache.containsKey(type)) {
            return getCachedValue(type);
        } else {
            T reference = supplier.get();
            cache.put(type, reference);
            return reference;
        }
    }

    public <T> Optional<T> getCachedValueOrElse(CacheElement type, ExceptionalSupplier<T> supplier, Consumer<Exception> consumer) {
        try {
            return Optional.of(getCachedValueOrElse(type, supplier));
        } catch (Exception e) {
            consumer.accept(e);
        }
        return Optional.empty();
    }

    public <T> Optional<T> getCachedValueOrElseRun(CacheElement type, Runnable runnable) {
        try {
            return Optional.of(getCachedValue(type));
        } catch (Exception e) {
            runnable.run();
        }
        return Optional.empty();
    }

    @Override
    public void accept(CachedValueLoader<CacheElement, ?> loader) {
        if (loader.getException() != null && loader.getValue() == null) {
            Logger.error("Failed to cache value for {}", loader.getType());
        } else {
            if (loader.getException() != null) {
                Logger.warn("Cache value for {} using fallback - root: {}", loader.getType(), loader.getException().getMessage());
            } else {
                Logger.info("Cache value for {}", loader.getType());
            }
            cache(loader.getType(), loader.getValue());
        }
    }

    @Override
    public boolean isCached(CacheElement element) {
        return cache.containsKey(element);
    }

    protected abstract LeagueClient getClient();
}
