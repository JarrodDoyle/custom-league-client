package com.hawolt.ui.layout;

/**
 * Created: 11/09/2023 22:58
 * Author: Twitter @hawolt
 **/

public enum LayoutComponent {
    HOME, STORE, PLAY, SELECT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
