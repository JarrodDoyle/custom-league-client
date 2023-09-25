package com.hawolt.client.resources.ledge.summoner.objects;

import org.json.JSONObject;

import java.util.Optional;

/**
 * Created: 24/09/2023 13:12
 * Author: Twitter @hawolt
 **/

public class SummonerProfile {
    private Regalia regalia;
    private int backgroundSkinId;

    public SummonerProfile(JSONObject object) {
        if (object.has("regalia")) {
            this.regalia = new Regalia(new JSONObject(object.getString("regalia")));
        }
        if (object.has("backgroundSkinId")) {
            this.backgroundSkinId = object.getInt("backgroundSkinId");
        }
    }

    public Optional<Regalia> getRegalia() {
        return Optional.ofNullable(regalia);
    }

    public int getBackgroundSkinId() {
        return backgroundSkinId;
    }
}
