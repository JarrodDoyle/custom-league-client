package com.hawolt.client.resources.ledge.preferences.objects;

import com.hawolt.virtual.misc.DynamicObject;
import org.json.JSONObject;


public abstract class DynamicPreferenceObject extends DynamicObject {
    public DynamicPreferenceObject(JSONObject o) {
        super(o);
    }

    protected JSONObject getDataSource() {
        return getJSONObject("data");
    }

    public int getSchemaVersion() {
        return getInt("schemaVersion");
    }
}
