package com.hawolt.client.exceptional;

/**
 * Created: 14/08/2023 17:43
 * Author: Twitter @hawolt
 **/

public interface ExceptionalSupplier<T> {
    T get() throws Exception;
}
