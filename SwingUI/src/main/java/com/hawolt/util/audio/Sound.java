package com.hawolt.util.audio;

/**
 * Created: 03/09/2023 19:11
 * Author: Twitter @hawolt
 **/

public enum Sound {
    ERROR("error.mp3"),
    SUCCESS("success.mp3"),
    QUEUE_POP("queue-pop.mp3"),
    ACTIVE_CHAT_MESSAGE("msg-1.mp3"),
    INACTIVE_CHAT_MESSAGE("msg-2.mp3"),
    FRIEND_REQUEST("friend-request.mp3"),
    OPEN_STORE("open-store.mp3"),
    PICK("pick.mp3");
    final String filename;

    Sound(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
