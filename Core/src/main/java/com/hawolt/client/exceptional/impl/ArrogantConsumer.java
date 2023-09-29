package com.hawolt.client.exceptional.impl;

import com.hawolt.client.exceptional.ExceptionalConsumer;
import com.hawolt.logger.Logger;

/**
 * Created: 25/09/2023 22:41
 * Author: Twitter @hawolt
 **/

public class ArrogantConsumer<T> {
    private final ExceptionalConsumer<T> original;

    public ArrogantConsumer(ExceptionalConsumer<T> consumer) {
        this.original = consumer;
    }

    public static <T> void consume(ArrogantConsumer<T> consumer, T t) {
        ArrogantConsumer.consume(consumer.getOriginal(), t);
    }

    public static <T> void consume(ExceptionalConsumer<T> consumer, T t) {
        try {
            consumer.consume(t);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public ExceptionalConsumer<T> getOriginal() {
        return original;
    }

    public void consume(T t) {
        ArrogantConsumer.consume(this, t);
    }
}
