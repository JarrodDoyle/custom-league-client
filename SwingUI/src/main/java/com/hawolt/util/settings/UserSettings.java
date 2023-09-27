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

    public JSONObject getPartyPositionPreference() {
        if (has("preferences")) {
            JSONObject prefs = getJSONObject("preferences");
            if (prefs.has("partiesPositionPreferences")) {
                JSONObject partyPosPrefs = prefs.getJSONObject("partiesPositionPreferences");
                if (partyPosPrefs.has("data")) {
                    return (partyPosPrefs.getJSONObject("data"));
                }
            }
        }
        return new JSONObject().put("firstPreference", "UNSELECTED").put("secondPreference", "UNSELECTED");
    }

    public JSONArray getChampSelectSpellPreference(int queueId) {
        if (has("preferences")) {
            JSONObject preferences = getJSONObject("preferences");
            if (preferences.has("champ-select")) {
                JSONObject champSelect = preferences.getJSONObject("champ-select");
                if (champSelect.has("data")) {
                    JSONObject data = champSelect.getJSONObject("data");
                    if (data.has("spells")) {
                        JSONObject spells = data.getJSONObject("spells");
                        String key = String.valueOf(queueId);
                        if (spells.has(key)) {
                            return spells.getJSONArray(key);
                        }
                    }
                }
            }
        }
        return new JSONArray().put(6).put(7);
    }

    public JSONObject setPartyPositionPreference(JSONObject data) {
        if (has("preferences")) {
            JSONObject prefs = getJSONObject("preferences");
            if (prefs.has("partiesPositionPreferences")) {
                JSONObject partiesPosPrefs = prefs.getJSONObject("partiesPositionPreferences");
                if (partiesPosPrefs.has("data")) {
                    JSONObject pppData = partiesPosPrefs.getJSONObject("data");
                    pppData.remove("firstPreference");
                    pppData.remove("secondPreference");
                    pppData.put("firstPreference", data.getString("firstPreference"));
                    pppData.put("secondPreference", data.getString("secondPreference"));
                } else {
                    partiesPosPrefs.put("data", new JSONObject().put("firstPreference", data.getString("firstPreference")).put("secondPreference", data.getString("secondPreference")));
                }
                prefs.put("partiesPositionPreferences", new JSONObject().put("data", new JSONObject().put("firstPreference", data.getString("firstPreference")).put("secondPreference", data.getString("secondPreference"))).put("schemaVersion", 0));
                prefs.getJSONObject("partiesPositionPreferences").put("data", new JSONObject().put("firstPreference", data.getString("firstPreference")).put("secondPreference", data.getString("secondPreference")));
            }
        }
        return getJSONObject("preferences");
    }

    public JSONObject setSummonerSpellPreference(int queueId, JSONArray preference) {
        if (has("preferences")) {
            JSONObject preferences = getJSONObject("preferences");
            if (preferences.has("champ-select")) {
                JSONObject champSelect = preferences.getJSONObject("champ-select");
                if (champSelect.has("data")) {
                    JSONObject data = champSelect.getJSONObject("data");
                    if (data.has("spells")) {
                        JSONObject spells = data.getJSONObject("spells");
                        String key = String.valueOf(queueId);
                        if (spells.has(key)) spells.remove(key);
                        spells.put(key, preference);
                        data.put("spells", spells);
                    }
                }
            }
        }
        return getJSONObject("preferences");
    }

}
