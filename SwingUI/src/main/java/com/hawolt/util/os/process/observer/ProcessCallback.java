package com.hawolt.util.os.process.observer;

/**
 * Created: 29/09/2023 15:13
 * Author: Twitter @hawolt
 **/

public interface ProcessCallback {
    enum State {
        UNKNOWN, STARTED, RUNNING, TERMINATED, NOT_RUNNING
    }

    void onStateChange(State state);
}
