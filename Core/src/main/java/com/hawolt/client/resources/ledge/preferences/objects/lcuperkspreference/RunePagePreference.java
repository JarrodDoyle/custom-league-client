package com.hawolt.client.resources.ledge.preferences.objects.lcuperkspreference;

import com.hawolt.client.resources.ledge.preferences.objects.DynamicPreferenceObject;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created: 01/10/2023 14:58
 * Author: Twitter @hawolt
 **/

public class RunePagePreference extends DynamicPreferenceObject {

    public RunePagePreference(JSONObject o) {
        super(o);
    }

    public JSONArray getSelectedPerkIds() {
        return getJSONArray("selectedPerkIds");
    }

    public Integer getPrimaryStyleId() {
        return getInt("primaryStyleId");
    }

    public Integer getSubStyleId() {
        return getInt("subStyleId");
    }

    public Integer getOrder() {
        return getInt("order");
    }

    public String getName() {
        return getString("name");
    }

    public Long getId() {
        return getLong("id");
    }
}
