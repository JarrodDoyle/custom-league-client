package com.hawolt.client.resources.communitydragon.companion;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created: 30/08/2023 20:00
 * Author: Twitter @hawolt
 **/

public class Companion {
    public final static String BASE = "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/assets/loadouts/companions";
    public static Companion DUMMY = new Companion(
            new JSONObject()
                    .put("contentId", "dummy")
                    .put("itemId", -1)
                    .put("name", "dummy")
                    .put("loadoutsIcon", "dummy")
                    .put("loadoutsIcon", "/dummy.jpg")
                    .put("description", "dummy")
                    .put("level", -1)
                    .put("speciesName", "dummy")
                    .put("speciesId", -1)
                    .put("rarity", "dummy")
                    .put("rarityValue", -1)
                    .put("isDefault", false)
                    .put("upgrades", new JSONArray())
                    .put("TFTOnly", false)
    );

    private final String contentId, name, description, loadoutsIcon, rarity, iconName, speciesName;
    private final int level, speciesId, rarityValue;
    private final boolean isDefault, tftOnly;
    private final JSONArray upgrades;
    private final long itemId;

    public Companion(JSONObject o) {
        this.contentId = o.getString("contentId");
        this.itemId = o.getLong("itemId");
        this.name = o.getString("name");
        String icon = o.getString("loadoutsIcon");
        this.iconName = icon.substring(icon.indexOf("Companions/") + 11).toLowerCase();
        this.loadoutsIcon = String.join("/", BASE, iconName);
        this.description = o.getString("description");
        this.level = o.getInt("level");
        this.speciesName = o.getString("speciesName");
        this.speciesId = o.getInt("speciesId");
        this.rarity = o.getString("rarity");
        this.rarityValue = o.getInt("rarityValue");
        this.isDefault = o.getBoolean("isDefault");
        this.upgrades = o.getJSONArray("upgrades");
        this.tftOnly = o.getBoolean("TFTOnly");
    }

    public String getContentId() {
        return contentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLoadoutsIcon() {
        return loadoutsIcon;
    }

    public String getRarity() {
        return rarity;
    }

    public String getIconName() {
        return iconName;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public int getLevel() {
        return level;
    }

    public int getSpeciesId() {
        return speciesId;
    }

    public int getRarityValue() {
        return rarityValue;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isTftOnly() {
        return tftOnly;
    }

    public JSONArray getUpgrades() {
        return upgrades;
    }

    public long getItemId() {
        return itemId;
    }

    @Override
    public String toString() {
        return name;
    }
}
