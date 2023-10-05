package com.hawolt.client.cache;

/**
 * Created: 24/09/2023 09:40
 * Author: Twitter @hawolt
 **/

public enum CacheElement {
    PUUID(CachedDataType.VALUE),
    ACCOUNT_ID(CachedDataType.VALUE),
    SUMMONER_ID(CachedDataType.VALUE),
    SUMMONER(CachedDataType.VALUE),
    RANKED_STATISTIC(CachedDataType.VALUE),
    INVENTORY_TOKEN(CachedDataType.JWT),
    MATCH_CONTEXT(CachedDataType.VALUE),
    LEAGUE_LEDGE_NOTIFICATION(CachedDataType.VALUE),
    PLAYER_PREFERENCE(CachedDataType.VALUE),
    PRESENCE(CachedDataType.VALUE),
    PROFILE(CachedDataType.VALUE),
    PROFILE_MASK(CachedDataType.VALUE),
    CHAMP_SELECT_COUNTER(CachedDataType.VALUE),
    LOGIN_DATA_PACKET(CachedDataType.VALUE),
    FREE_TO_PLAY_LEVEL_CAP(CachedDataType.VALUE),
    F2P_NEW_PLAYER(CachedDataType.VALUE),
    F2P_VETERAN_PLAYER(CachedDataType.VALUE),
    PARTY_REGISTRATION(CachedDataType.VALUE),
    LCU_PREFERENCES(CachedDataType.VALUE),
    LCU_SOCIAL_PREFERENCES(CachedDataType.VALUE),
    STORE_CATALOG(CachedDataType.VALUE),
    PERSONALIZED_OFFERS(CachedDataType.VALUE),
    GAME_CREDENTIALS(CachedDataType.VALUE),
    PERKS_PREFERENCE(CachedDataType.VALUE);

    private final CachedDataType cachedDataType;

    CacheElement(CachedDataType cachedDataType) {
        this.cachedDataType = cachedDataType;
    }

    public CachedDataType getCachedDataType() {
        return cachedDataType;
    }
}
