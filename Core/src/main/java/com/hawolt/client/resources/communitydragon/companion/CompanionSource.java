package com.hawolt.client.resources.communitydragon.companion;

import com.hawolt.client.resources.communitydragon.CommunityDragonSource;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import okhttp3.Request;

import java.io.IOException;

/**
 * Created: 29/08/2023 20:11
 * Author: Twitter @hawolt
 **/

public class CompanionSource implements CommunityDragonSource<CompanionIndex> {

    public static final CompanionSource COMPANION_SOURCE = new CompanionSource();

    private CompanionIndex cache;

    @Override
    public String getSource(String... args) {
        return "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/companions.json";
    }

    @Override
    public CompanionIndex get(String... args) {
        if (cache != null) return cache;
        Request request = new Request.Builder()
                .url(getSource())
                .header("User-Agent", "hawolt-custom-client-core")
                .get()
                .build();
        try {
            IResponse response = OkHttp3Client.execute(request);
            cache = new CompanionIndex(response);
        } catch (IOException e) {
            Logger.warn("failed to load {}", getClass().getSimpleName());
        }
        return cache != null ? cache : new CompanionIndex();
    }
}
