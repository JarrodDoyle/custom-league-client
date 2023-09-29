package com.hawolt.client.resources.communitydragon.tftmapskin;

import com.hawolt.http.layer.IResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created: 29/08/2023 21:29
 * Author: Twitter @hawolt
 **/

public class TFTMapSkinIndex {

    private final Map<Long, TFTMapSkin> map = new HashMap<>();

    public TFTMapSkinIndex() {

    }

    public TFTMapSkinIndex(IResponse response) {
        JSONArray array = new JSONArray(response.asString());
        for (int i = 0; i < array.length(); i++) {
            JSONObject reference = array.getJSONObject(i);
            TFTMapSkin tftMapSkin = new TFTMapSkin(reference);
            map.put(tftMapSkin.getItemId(), tftMapSkin);
        }
    }

    public TFTMapSkin[] getAvailableTFTMapSkin() {
        return map.values().toArray(new TFTMapSkin[0]);
    }

    public TFTMapSkin getTFTMapSkin(long id) {
        return map.getOrDefault(id, TFTMapSkin.DUMMY);
    }
}
