package com.hawolt.ui.layout;

/**
 * Created: 11/09/2023 22:58
 * Author: Twitter @hawolt
 **/

public enum LayoutComponent {
    HOME(true),
    STORE(true),
    PLAY(true),
    PROFILE(true),
    CHAMPSELECT(true),
    YOUR_SHOP(false),
    RECONNECT(false);
    private final boolean defaultComponent;

    LayoutComponent(boolean defaultComponent) {
        this.defaultComponent = defaultComponent;
    }

    public boolean isDefaultComponent() {
        return defaultComponent;
    }

    public String getPrettyName() {
        return name().replace("_", " ");
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
