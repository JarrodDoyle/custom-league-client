package com.hawolt.client.cache;

import com.hawolt.logger.Logger;

import java.util.function.Consumer;

/**
 * Created: 14/08/2023 17:42
 * Author: Twitter @hawolt
 **/

public class CachedValueLoader<S, T> implements Runnable {
    private final Consumer<CachedValueLoader<S, ?>> consumer;
    private final ExceptionalSupplier<T> supplier;
    private final S type;
    private Exception e;
    private T value;

    public CachedValueLoader(S type, ExceptionalSupplier<T> supplier, Consumer<CachedValueLoader<S, ?>> consumer) {
        this.consumer = consumer;
        this.supplier = supplier;
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
            this.e = e;
        }
        this.consumer.accept(this);
    }
}
