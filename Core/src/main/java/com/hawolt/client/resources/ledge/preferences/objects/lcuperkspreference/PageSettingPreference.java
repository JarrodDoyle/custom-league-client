package com.hawolt.client.resources.ledge.preferences.objects.lcuperkspreference;

import com.hawolt.client.resources.ledge.preferences.objects.DynamicPreferenceObject;
import com.hawolt.generic.data.Platform;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Created: 01/10/2023 14:58
 * Author: Twitter @hawolt
 **/

public class PageSettingPreference extends DynamicPreferenceObject {
    public PageSettingPreference(JSONObject o) {
        super(o);
    }

    public Optional<ShardPageSettingPreference> getShardPageSettingPreference(Platform platform) {
        if (!getDataSource().has("perShardPerkBooks")) return Optional.empty();
        JSONObject perShardPerkBooks = getDataSource().getJSONObject("perShardPerkBooks");
        if (!perShardPerkBooks.has(platform.name().toLowerCase())) return Optional.empty();
        return Optional.of(new ShardPageSettingPreference(perShardPerkBooks.getJSONObject(platform.name().toLowerCase())));
    }

}
