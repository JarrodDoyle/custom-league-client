package com.hawolt.client.cache;

import com.hawolt.client.exceptional.ExceptionalSupplier;
import com.hawolt.logger.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created: 14/08/2023 17:42
 * Author: Twitter @hawolt
 **/

public class CachedValueLoader<S, T> implements Runnable {
    private final Consumer<CachedValueLoader<S, ?>> consumer;
    private final ExceptionalSupplier<T> supplier;
    private final Supplier<T> fallback;
    private final S type;
    private Exception e;
    private T value;

    public CachedValueLoader(S type, ExceptionalSupplier<T> supplier, Consumer<CachedValueLoader<S, ?>> consumer) {
        this(type, supplier, null, consumer);
    }

    public CachedValueLoader(S type, ExceptionalSupplier<T> supplier, Supplier<T> fallback, Consumer<CachedValueLoader<S, ?>> consumer) {
        this.consumer = consumer;
        this.supplier = supplier;
        this.fallback = fallback;
        this.type = type;
    }

    public S getType() {
        return type;
    }

    public Exception getException() {
        return e;
    }

    public T getValue() {
        return value;
    }

    @Override
    public void run() {
        Logger.info("Caching value for {}", type);
        try {
            this.value = supplier.get();
        } catch (Exception e) {
            if (fallback != null) this.value = fallback.get();
            this.e = e;
        }
        this.consumer.accept(this);
    }
}
