package com.hawolt.client.resources.communitydragon.tftmapskin;

import com.hawolt.client.resources.communitydragon.CommunityDragonSource;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import okhttp3.Request;

/**
 * Created: 29/08/2023 20:11
 * Author: Twitter @hawolt
 **/

public class TFTMapSkinSource implements CommunityDragonSource<TFTMapSkinIndex> {

    public static final TFTMapSkinSource TFT_MAP_SKIN_SOURCE = new TFTMapSkinSource();

    private TFTMapSkinIndex cache;

    @Override
    public String getSource(String... args) {
        return "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/tftmapskins.json";
    }

    @Override
    public TFTMapSkinIndex get(String... args) {
        if (cache != null) return cache;
        Request request = new Request.Builder()
                .url(getSource())
                .header("User-Agent", "hawolt-custom-client-core")
                .get()
                .build();
        try {
            IResponse response = OkHttp3Client.execute(request);
            cache = new TFTMapSkinIndex(response);
        } catch (Exception e) {
            Logger.warn("failed to load {}", getClass().getSimpleName());
        }
        return cache != null ? cache : new TFTMapSkinIndex();
    }
}
