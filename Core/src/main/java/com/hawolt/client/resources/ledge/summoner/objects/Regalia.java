package com.hawolt.client.resources.ledge.summoner.objects;

import org.json.JSONObject;

/**
 * Created: 24/09/2023 13:13
 * Author: Twitter @hawolt
 **/

public class Regalia {
    public final int bannerType, crestType, selectedPrestigeCrest;

    public Regalia(JSONObject object) {
        this.selectedPrestigeCrest = object.getInt("selectedPrestigeCrest");
        this.bannerType = object.getInt("bannerType");
        this.crestType = object.getInt("crestType");
    }

    public int getBannerType() {
        return bannerType;
    }

    public int getCrestType() {
        return crestType;
    }

    public int getSelectedPrestigeCrest() {
        return selectedPrestigeCrest;
    }
}
