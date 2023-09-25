package com.hawolt.client.exceptional;

/**
 * Created: 25/09/2023 22:35
 * Author: Twitter @hawolt
 **/

public interface ExceptionalConsumer<T> {
    void consume(T t) throws Exception;
}
