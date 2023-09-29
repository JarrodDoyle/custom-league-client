package com.hawolt.client.resources.communitydragon.tftmapskin;

import org.json.JSONObject;

/**
 * Created: 30/08/2023 20:00
 * Author: Twitter @hawolt
 **/

public class TFTMapSkin {
    public final static String BASE = "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/assets/loadouts/tftmapskins";
    public static TFTMapSkin DUMMY = new TFTMapSkin(
            new JSONObject()
                    .put("contentId", "dummy")
                    .put("itemId", -1)
                    .put("name", "dummy")
                    .put("description", "dummy")
                    .put("loadoutsIcon", "/dummy.jpg")
                    .put("groupId", -1)
                    .put("groupName", "dummy")
                    .put("rarity", "dummy")
                    .put("rarityValue", -1)
    );

    private final String contentId, name, description, loadoutsIcon, groupName, rarity, iconName;
    private final int groupId, rarityValue;
    private final long itemId;

    public TFTMapSkin(JSONObject o) {
        this.contentId = o.getString("contentId");
        this.itemId = o.getLong("itemId");
        this.name = o.getString("name");
        this.description = o.getString("description");
        String icon = o.getString("loadoutsIcon");
        this.iconName = icon.substring(icon.lastIndexOf("/") + 1).toLowerCase();
        this.loadoutsIcon = String.join("/", BASE, iconName);
        this.groupId = o.getInt("groupId");
        this.groupName = o.getString("groupName");
        this.rarity = o.getString("rarity");
        this.rarityValue = o.getInt("rarityValue");
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

    public String getGroupName() {
        return groupName;
    }

    public String getRarity() {
        return rarity;
    }

    public long getItemId() {
        return itemId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getRarityValue() {
        return rarityValue;
    }

    @Override
    public String toString() {
        return name;
    }
}
