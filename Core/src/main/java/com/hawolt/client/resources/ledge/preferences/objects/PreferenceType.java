package com.hawolt.client.resources.ledge.preferences.objects;

/**
 * Created: 11/09/2023 16:47
 * Author: Twitter @hawolt
 **/

public enum PreferenceType {
    LCU_SOCIAL_PREFERENCES("LcuSocialPreferences", PreferenceDataType.YAML),
    PERKS_PREFERENCE("PerksPreferences", PreferenceDataType.YAML),
    LCU_PREFERENCES("LCUPreferences", PreferenceDataType.YAML);

    final String name;
    final PreferenceDataType data;

    PreferenceType(String name, PreferenceDataType data) {
        this.name = name;
        this.data = data;
    }

    public PreferenceDataType getDataType() {
        return data;
    }

    public String getName() {
        return name;
    }
}
