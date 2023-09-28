package com.hawolt.util.settings;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created: 28/08/2023 19:49
 * Author: Twitter @hawolt
 **/

public class UserSettings extends DynamicSettings {
    public UserSettings(JSONObject o, SettingService service) {
        super(o, service);
    }

    public JSONArray getCookies() {
        return getByKeyOrDefault("cookies", new JSONArray());
    }


    public JSONObject setSummonerSpellPreference(int queueId, JSONArray preference) {
        if (has("preferences")) {

        }
        return getJSONObject("preferences");
    }

}
