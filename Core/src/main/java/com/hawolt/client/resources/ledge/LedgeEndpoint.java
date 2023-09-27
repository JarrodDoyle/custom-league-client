package com.hawolt.client.resources.ledge;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.challenges.ChallengeLedge;
import com.hawolt.client.resources.ledge.championmastery.ChampionMasteryLedge;
import com.hawolt.client.resources.ledge.gsm.GameServiceMessageLedge;
import com.hawolt.client.resources.ledge.inventory.InventoryServiceLedge;
import com.hawolt.client.resources.ledge.leagues.LeagueLedge;
import com.hawolt.client.resources.ledge.loot.LootLedge;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.perks.PerksLedge;
import com.hawolt.client.resources.ledge.personalizedoffers.PersonalizedOffersLedge;
import com.hawolt.client.resources.ledge.preferences.PlayerPreferencesLedge;
import com.hawolt.client.resources.ledge.store.StoreLedge;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.teambuilder.TeamBuilderLedge;
import com.hawolt.client.resources.ledge.unclassified.UnclassifiedLedge;

/**
 * Created: 19/01/2023 16:03
 * Author: Twitter @hawolt
 **/

public class LedgeEndpoint {
    private final GameServiceMessageLedge gameServiceMessageLedge;
    private final PersonalizedOffersLedge personalizedOffersLedge;
    private final PlayerPreferencesLedge playerPreferencesLedge;
    private final InventoryServiceLedge inventoryServiceLedge;
    private final ChampionMasteryLedge championMasteryLedge;
    private final UnclassifiedLedge unclassifiedLedge;
    private final TeamBuilderLedge teamBuilderLedge;
    private final ChallengeLedge challengeLedge;
    private final SummonerLedge summonerLedge;
    private final PartiesLedge partiesLedge;
    private final LeagueLedge leagueLedge;
    private final StoreLedge storeLedge;
    private final PerksLedge perksLedge;
    private final LootLedge lootLedge;

    public LedgeEndpoint(LeagueClient client) {
        this.lootLedge = new LootLedge(client);
        this.storeLedge = new StoreLedge(client);
        this.perksLedge = new PerksLedge(client);
        this.leagueLedge = new LeagueLedge(client);
        this.partiesLedge = new PartiesLedge(client);
        this.summonerLedge = new SummonerLedge(client);
        this.challengeLedge = new ChallengeLedge(client);
        this.teamBuilderLedge = new TeamBuilderLedge(client);
        this.unclassifiedLedge = new UnclassifiedLedge(client);
        this.championMasteryLedge = new ChampionMasteryLedge(client);
        this.inventoryServiceLedge = new InventoryServiceLedge(client);
        this.playerPreferencesLedge = new PlayerPreferencesLedge(client);
        this.gameServiceMessageLedge = new GameServiceMessageLedge(client);
        this.personalizedOffersLedge = new PersonalizedOffersLedge(client);
    }

    public PersonalizedOffersLedge getPersonalizedOffers() {
        return personalizedOffersLedge;
    }

    public GameServiceMessageLedge getGameServiceMessage() {
        return gameServiceMessageLedge;
    }

    public InventoryServiceLedge getInventoryService() {
        return inventoryServiceLedge;
    }

    public SummonerLedge getSummoner() {
        return summonerLedge;
    }

    public PartiesLedge getParties() {
        return partiesLedge;
    }

    public LeagueLedge getLeague() {
        return leagueLedge;
    }

    public TeamBuilderLedge getTeamBuilder() {
        return teamBuilderLedge;
    }

    public LootLedge getLoot() {
        return lootLedge;
    }

    public StoreLedge getStore() {
        return storeLedge;
    }

    public PerksLedge getPerks() {
        return perksLedge;
    }

    public ChampionMasteryLedge getChampionMastery() {
        return championMasteryLedge;
    }

    public PlayerPreferencesLedge getPlayerPreferences() {
        return playerPreferencesLedge;
    }

    public UnclassifiedLedge getUnclassified() {
        return unclassifiedLedge;
    }

    public ChallengeLedge getChallenge() {
        return challengeLedge;
    }
}
