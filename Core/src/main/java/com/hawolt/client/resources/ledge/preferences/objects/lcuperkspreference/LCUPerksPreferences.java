package com.hawolt.client.resources.ledge.preferences.objects.lcuperkspreference;

import com.hawolt.logger.Logger;
import com.hawolt.virtual.misc.DynamicObject;
import org.json.JSONObject;

import java.util.Optional;

public class LCUPerksPreferences extends DynamicObject {
    private PageSettingPreference pageSettingPreference;

    public LCUPerksPreferences(JSONObject o) {
        super(o);
        // keep this temporary until all platforms are confirmed working with this approach ~ hawolt
        Logger.debug(o);
    }

    public Optional<PageSettingPreference> getPerksPageSettings() {
        if (pageSettingPreference != null) return Optional.of(pageSettingPreference);
        if (has("champ-select")) {
            this.pageSettingPreference = new PageSettingPreference(getByKey("page-settings"));
        } else {
            JSONObject page = new JSONObject().put("data", new JSONObject()).put("schemaVersion", 5);
            this.pageSettingPreference = new PageSettingPreference(page);
            this.put("page-settings", page);
        }
        return Optional.ofNullable(pageSettingPreference);
    }
}
