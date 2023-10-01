package com.hawolt.client.resources.ledge.preferences.objects.lcuperkspreference;

import com.hawolt.client.resources.ledge.preferences.objects.DynamicPreferenceObject;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created: 01/10/2023 14:58
 * Author: Twitter @hawolt
 **/

public class ShardPageSettingPreference extends DynamicPreferenceObject {

    public ShardPageSettingPreference(JSONObject o) {
        super(o);
    }

    public long getCurrentPageId() {
        return getLong("currentPageId");
    }

    public RunePagePreference[] getRunePagePreferences() {
        JSONArray pages = has("pages") ? getJSONArray("pages") : new JSONArray();
        RunePagePreference[] preferences = new RunePagePreference[pages.length()];
        for (int i = 0; i < pages.length(); i++) {
            preferences[i] = new RunePagePreference(pages.getJSONObject(i));
        }
        return preferences;
    }
}
