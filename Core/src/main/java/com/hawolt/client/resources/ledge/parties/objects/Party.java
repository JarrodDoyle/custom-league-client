package com.hawolt.client.resources.ledge.parties.objects;

import org.json.JSONObject;

/**
 * Created: 20/08/2023 10:47
 * Author: Twitter @hawolt
 **/

public class Party {
    protected final String puuid, platformId, partyId, role;
    protected final long accountId, summonerId;
    protected final int partyVersion;

    public Party(JSONObject object) {
        //Logger.error(object);
        this.puuid = object.getString("puuid");
        this.platformId = object.getString("platformId");
        this.accountId = object.getLong("accountId");
        this.summonerId = object.getLong("summonerId");
        this.partyId = object.getString("partyId");
        this.partyVersion = object.getInt("partyVersion");
        this.role = object.getString("role");
    }

    public String getPUUID() {
        return puuid;
    }

    public String getPlatformId() {
        return platformId;
    }

    public String getPartyId() {
        return partyId;
    }

    public String getRole() {
        return role;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getSummonerId() {
        return summonerId;
    }

    public int getPartyVersion() {
        return partyVersion;
    }

    @Override
    public String toString() {
        return "Party{" +
                "puuid='" + puuid + '\'' +
                ", platformId='" + platformId + '\'' +
                ", partyId='" + partyId + '\'' +
                ", role='" + role + '\'' +
                ", accountId=" + accountId +
                ", summonerId=" + summonerId +
                ", partyVersion=" + partyVersion +
                '}';
    }
}
