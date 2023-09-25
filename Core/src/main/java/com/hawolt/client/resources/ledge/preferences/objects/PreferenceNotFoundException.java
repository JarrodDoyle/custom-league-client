package com.hawolt.client.resources.ledge.preferences.objects;

/**
 * Created: 25/09/2023 20:53
 * Author: Twitter @hawolt
 **/

public class PreferenceNotFoundException extends Exception {
    private final String origin;

    public PreferenceNotFoundException(String origin) {
        super(origin);
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }
}
