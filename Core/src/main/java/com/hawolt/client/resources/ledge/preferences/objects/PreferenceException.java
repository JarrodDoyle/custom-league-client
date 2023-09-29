package com.hawolt.client.resources.ledge.preferences.objects;

public class PreferenceException extends RuntimeException {
    public PreferenceException(String name) {
        super(String.format("Unable to find preference for %s", name));
    }
}
