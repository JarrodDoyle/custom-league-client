package com.hawolt.client.resources.ledge.preferences.objects.lcupreferences;

import com.hawolt.client.resources.ledge.preferences.objects.DynamicPreferenceObject;
import org.json.JSONObject;


public class PartiesPositionPreference extends DynamicPreferenceObject {
    public PartiesPositionPreference(JSONObject o) {
        super(o);
    }

    //TODO I have no idea how to handle Optionals properly, ask hawolt i guess
    public String getFirstPreference() {
        return getDataSource().getString("firstPreference");
    }

    public void setFirstPreference(String firstPreference) {
        getDataSource().put("firstPreference", firstPreference);
    }

    public String getSecondPreference() {
        return getDataSource().getString("secondPreference");
    }

    public void setSecondPreference(String secondPreference) {
        getDataSource().put("secondPreference", secondPreference);
    }

}
