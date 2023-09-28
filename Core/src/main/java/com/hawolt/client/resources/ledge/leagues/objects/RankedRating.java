package com.hawolt.client.resources.ledge.leagues.objects;

import org.json.JSONObject;

import java.util.Optional;

/**
 * Created: 24/09/2023 03:13
 * Author: Twitter @hawolt
 **/

public class RankedRating {
    private final int provisionalGameThreshold, leaguePoints, wins, losses, provisionalGamesRemaining, ratedRating;
    private final boolean premadeMmrRestricted;
    private final String tier, rank, highestTier, highestRank, previousSeasonEndTier, previousSeasonEndRank;
    private QueueType queueType;

    public RankedRating(JSONObject object) {
        this.queueType = QueueType.valueOf(object.getString("queueType"));
        this.premadeMmrRestricted = object.getBoolean("premadeMmrRestricted");
        this.provisionalGameThreshold = object.getInt("provisionalGameThreshold");
        this.leaguePoints = object.getInt("leaguePoints");
        this.wins = object.getInt("wins");
        this.losses = object.getInt("losses");
        this.provisionalGamesRemaining = object.getInt("provisionalGamesRemaining");
        this.ratedRating = object.getInt("ratedRating");
        this.tier = getOrNull(object, "tier");
        this.rank = getOrNull(object, "rank");
        this.highestTier = getOrNull(object, "highestTier");
        this.highestRank = getOrNull(object, "highestRank");
        this.previousSeasonEndTier = getOrNull(object, "previousSeasonEndTier");
        this.previousSeasonEndRank = getOrNull(object, "previousSeasonEndRank");
    }

    private String getOrNull(JSONObject reference, String key) {
        if (!reference.has(key) || reference.isNull(key)) return null;
        return reference.getString(key);
    }

    public QueueType getQueueType() {
        return queueType;
    }

    public int getProvisionalGameThreshold() {
        return provisionalGameThreshold;
    }

    public int getLeaguePoints() {
        return leaguePoints;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getProvisionalGamesRemaining() {
        return provisionalGamesRemaining;
    }

    public int getRatedRating() {
        return ratedRating;
    }

    public boolean isPremadeMmrRestricted() {
        return premadeMmrRestricted;
    }

    public Optional<String> getTier() {
        return Optional.ofNullable(tier);
    }

    public Optional<String> getRank() {
        return Optional.ofNullable(rank);
    }

    public Optional<String> getHighestTier() {
        return Optional.ofNullable(highestTier);
    }

    public Optional<String> getHighestRank() {
        return Optional.ofNullable(highestRank);
    }

    public Optional<String> getPreviousSeasonEndTier() {
        return Optional.ofNullable(previousSeasonEndTier);
    }

    public Optional<String> getPreviousSeasonEndRank() {
        return Optional.ofNullable(previousSeasonEndRank);
    }

    @Override
    public String toString() {
        return "RankedRating{" +
                "queueType=" + queueType +
                ", provisionalGameThreshold=" + provisionalGameThreshold +
                ", leaguePoints=" + leaguePoints +
                ", wins=" + wins +
                ", losses=" + losses +
                ", provisionalGamesRemaining=" + provisionalGamesRemaining +
                ", ratedRating=" + ratedRating +
                ", premadeMmrRestricted=" + premadeMmrRestricted +
                ", tier='" + tier + '\'' +
                ", rank='" + rank + '\'' +
                ", highestTier='" + highestTier + '\'' +
                ", highestRank='" + highestRank + '\'' +
                ", previousSeasonEndTier='" + previousSeasonEndTier + '\'' +
                ", previousSeasonEndRank='" + previousSeasonEndRank + '\'' +
                '}';
    }
}
