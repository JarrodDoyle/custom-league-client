package com.hawolt.client.resources.communitydragon.companion;

import com.hawolt.http.layer.IResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created: 29/08/2023 21:29
 * Author: Twitter @hawolt
 **/

public class CompanionIndex {

    private final Map<Long, Companion> map = new HashMap<>();

    public CompanionIndex() {

    }

    public CompanionIndex(IResponse response) {
        JSONArray array = new JSONArray(response.asString());
        for (int i = 0; i < array.length(); i++) {
            JSONObject reference = array.getJSONObject(i);
            Companion companion = new Companion(reference);
            map.put(companion.getItemId(), companion);
        }
    }

    public Companion[] getAvailableCompanions() {
        return map.values().toArray(new Companion[0]);
    }

    public Companion getCompanion(long id) {
        return map.getOrDefault(id, Companion.DUMMY);
    }
}
