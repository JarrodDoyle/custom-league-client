package com.hawolt.client.resources.ledge.preferences.objects.lcusocialpreferences;

import com.hawolt.virtual.misc.DynamicObject;
import org.json.JSONObject;

import java.util.Optional;

public class LCUSocialPreferences extends DynamicObject {
    public LCUSocialPreferences(JSONObject o) {
        super(o);
    }

    public Optional<String> getChatStatusMessage() {
        return Optional.ofNullable(this.getString("chat-status-message"));
    }

}
