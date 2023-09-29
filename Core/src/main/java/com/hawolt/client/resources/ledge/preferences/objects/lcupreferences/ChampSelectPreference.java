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
            JSONObject spells = getDataSource().getJSONObject("spells");
            if (spells.has(String.valueOf(queueId))) {
                JSONArray stored = spells.getJSONArray(String.valueOf(queueId));
                if (stored.length() == 2) return stored;
            }
        }
        return new JSONArray().put(6).put(7);
    }

    public void setSummonerSpells(int queueId, JSONArray spells) {
        if (!getDataSource().has("spells")) {
            getDataSource().put("spells", new JSONObject());
        }
        String queue = String.valueOf(queueId);
        JSONObject reference = getDataSource().getJSONObject("spells");
        if (reference.has(queue)) {
            reference.remove(queue);
        }
        reference.put(String.valueOf(queueId), spells);
    }

}
