package com.hawolt.client.cache;

/**
 * Created: 24/09/2023 09:40
 * Author: Twitter @hawolt
 **/

public enum CacheElement {
    PUUID(CachedDataType.VALUE),
    ACCOUNT_ID(CachedDataType.VALUE),
    SUMMONER_ID(CachedDataType.VALUE),
    PROFILE(CachedDataType.VALUE),
    RANKED_STATISTIC(CachedDataType.VALUE),
    INVENTORY_TOKEN(CachedDataType.JWT),
    CHAT_STATUS(CachedDataType.VALUE),
    MATCH_CONTEXT(CachedDataType.VALUE),
    LEAGUE_LEDGE_NOTIFICATION(CachedDataType.VALUE),
    PLAYER_PREFERENCE(CachedDataType.VALUE),
    PRESENCE(CachedDataType.VALUE);
    private final CachedDataType cachedDataType;

    CacheElement(CachedDataType cachedDataType) {
        this.cachedDataType = cachedDataType;
    }

    public CachedDataType getCachedDataType() {
        return cachedDataType;
    }
}
