package com.hawolt.util.os.process.observer;

/**
 * Created: 29/09/2023 15:13
 * Author: Twitter @hawolt
 **/

public interface ProcessCallback {
    void onStateChange(State state);

    enum State {
        UNKNOWN, STARTED, RUNNING, TERMINATED, NOT_RUNNING
    }
}
