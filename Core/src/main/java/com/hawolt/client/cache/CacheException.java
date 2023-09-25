package com.hawolt.client.cache;

/**
 * Created: 12/09/2023 09:24
 * Author: Twitter @hawolt
 **/

public class CacheException extends RuntimeException {
    private final Object reference;

    public CacheException(Object o) {
        super("Unable to associate value from key " + o.toString());
        this.reference = o;
    }

    public Object getReference() {
        return reference;
    }
}
