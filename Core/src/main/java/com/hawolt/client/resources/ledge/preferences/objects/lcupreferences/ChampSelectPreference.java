package com.hawolt.client.resources.ledge.preferences.objects.lcupreferences;

import com.hawolt.client.resources.ledge.preferences.objects.DynamicPreferenceObject;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChampSelectPreference extends DynamicPreferenceObject {
    public ChampSelectPreference(JSONObject o) {
        super(o);
    }

    public JSONArray getSummonerSpells(int queueId) {
        if (getDataSource().has("spells")) {
            if (getDataSource().getJSONObject("spells").has(String.valueOf(queueId))) {
                return getDataSource().getJSONObject("spells").getJSONArray(String.valueOf(queueId));
            }
        }
        return new JSONArray().put(6).put(7);
    }

    public void setSummonerSpells(int queueId, JSONArray spells) {
        if (!getDataSource().has("spells")) {
            getDataSource().put("spells", new JSONObject());
        }
        getDataSource().getJSONObject("spells").put(String.valueOf(queueId), spells);
    }

}
