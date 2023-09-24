package com.hawolt.ui.champselect.context.impl;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.ui.champselect.ChampSelectUI;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectContextProvider;
import com.hawolt.ui.champselect.context.ChampSelectInterfaceContext;
import com.hawolt.ui.champselect.generic.ChampSelectRuneSelection;
import com.hawolt.version.local.LocalLeagueFileVersion;

/**
 * Created: 10/09/2023 03:29
 * Author: Twitter @hawolt
 **/

public class ChampSelectInterface extends ChampSelectContextProvider implements ChampSelectInterfaceContext {
    public ChampSelectInterface(ChampSelectUI champSelectUI, ChampSelectContext context) {
        super(champSelectUI, context);
    }

    @Override
    public ChampSelectRuneSelection getRuneSelectionPanel() {
        LeagueClient client = champSelectUI.getLeagueClient();
        if (client != null) {
            LocalLeagueFileVersion leagueFileVersion = client.getVirtualLeagueClientInstance().getLocalLeagueFileVersion();
            String value = leagueFileVersion.getVersionValue(client.getPlayerPlatform(), "LeagueClientUxRender.exe");
            String[] versions = value.split("\\.");
            String patch = String.format("%s.%s.1", versions[0], versions[1]);
            return new ChampSelectRuneSelection(patch);
        } else {
            return new ChampSelectRuneSelection("13.17.1");
        }
    }

    @Override
    public void filterChampion(String champion) {
        this.champSelectUI.getInstance(context.getChampSelectSettingsContext().getQueueId()).invokeChampionFilter(champion);
    }

    @Override
    public LeagueClientUI getLeagueClientUI() {
        return champSelectUI.getLeagueClientUI();
    }
}
