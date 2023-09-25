package com.hawolt.ui.profile;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.leagues.LeagueLedge;
import com.hawolt.client.resources.ledge.leagues.objects.RankedStatistic;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.client.resources.ledge.summoner.objects.SummonerProfile;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.profile.overview.ProfileContainer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 24/09/2023 14:05
 * Author: Twitter @hawolt
 **/

public class ProfileWindow extends ChildUIComponent {
    private final Map<String, ProfileContainer> reference = new HashMap<>();
    private final CardLayout layout = new CardLayout();
    private final LeagueClientUI leagueClientUI;
    private final ChildUIComponent center;
    private final ProfileHeader header;

    private ProfileContainer currentProfileContainer;

    public ProfileWindow(LeagueClientUI leagueClientUI) {
        super(new BorderLayout());
        this.setOpaque(false);
        this.leagueClientUI = leagueClientUI;
        this.add(header = new ProfileHeader(), BorderLayout.NORTH);
        this.add(center = new ChildUIComponent(layout), BorderLayout.CENTER);
        this.header.getSearchField().addActionListener(listener -> {
            final String lookup = header.getSearchField().getText();
            LeagueClientUI.service.execute(() -> show(lookup));
            this.header.getSearchField().setText("");
        });
        this.header.getOverviewButton().addActionListener(listener -> {
            if (currentProfileContainer == null) return;
            currentProfileContainer.toggle("overview");
        });
        this.header.getHistoryButton().addActionListener(listener -> {
            if (currentProfileContainer == null) return;
            currentProfileContainer.toggle("history");
        });
        this.center.setOpaque(false);
        this.configure();
    }

    private void configure() {
        LeagueClient leagueClient = leagueClientUI.getLeagueClient();
        LeagueClientUI.service.execute(() -> {
            RankedStatistic rankedStatistic = leagueClient.getCachedValue(CacheElement.RANKED_STATISTIC);
            BufferedImage mask = leagueClient.getCachedValue(CacheElement.PROFILE_MASK);
            Summoner summoner = leagueClient.getCachedValue(CacheElement.SUMMONER);
            SummonerProfile profile = leagueClient.getCachedValue(CacheElement.PROFILE);
            ProfileContainer profileContainer = new ProfileContainer(mask, summoner, profile, rankedStatistic);
            this.setup(leagueClient.getCachedValue(CacheElement.PUUID), profileContainer);
        });
    }

    public void show(String name) {
        LeagueClient leagueClient = leagueClientUI.getLeagueClient();
        BufferedImage mask = leagueClient.getCachedValue(CacheElement.PROFILE_MASK);
        LeagueClientUI.service.execute(() -> {
            try {
                SummonerLedge summonerLedge = leagueClient.getLedge().getSummoner();
                LeagueLedge leagueLedge = leagueClient.getLedge().getLeague();
                Summoner summoner = summonerLedge.resolveSummonerByName(name);
                RankedStatistic rankedStatistic = leagueLedge.getRankedStats(summoner.getPUUID());
                SummonerProfile profile = summonerLedge.resolveSummonerProfile(summoner.getPUUID());
                ProfileContainer profileContainer = new ProfileContainer(mask, summoner, profile, rankedStatistic);
                this.setup(summoner.getPUUID(), profileContainer);
            } catch (Exception e) {
                Logger.error(e);
            }
        });
    }

    private void setup(String puuid, ProfileContainer profileContainer) {
        ProfileWindow.this.currentProfileContainer = profileContainer;
        ProfileWindow.this.reference.put(puuid, profileContainer);
        ProfileWindow.this.center.add(puuid, profileContainer);
        ProfileWindow.this.layout.show(center, puuid);
        ProfileWindow.this.repaint();
    }
}
