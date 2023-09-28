package com.hawolt.async.gsm;

import com.hawolt.Swiftrift;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.preferences.objects.PreferenceType;
import com.hawolt.client.resources.ledge.preferences.objects.lcupreferences.LCUPreferences;
import com.hawolt.event.EventListener;
import com.hawolt.event.impl.GameStartEvent;
import com.hawolt.logger.Logger;
import com.hawolt.ui.champselect.context.impl.ChampSelect;
import com.hawolt.ui.champselect.data.ChampSelectTeamMember;
import org.json.JSONArray;

import java.io.IOException;

/**
 * Created: 26/09/2023 18:15
 * Author: Twitter @hawolt
 **/

public class GameStartHandler implements EventListener<GameStartEvent> {
    private final Swiftrift swiftrift;

    public GameStartHandler(Swiftrift swiftrift) {
        this.swiftrift = swiftrift;
    }

    @Override
    public void onEvent(String s, GameStartEvent gameStartEvent) throws Exception {
        if (swiftrift.getLeagueClient().getLedge().getParties().enteredGame()) {
            Logger.debug("[gsl] game has been entered");
        } else {
            Logger.debug("[gsl] failed to notify game enter");
        }
        ChampSelect champSelect = swiftrift.getLayoutManager().getChampSelectUI().getChampSelect();
        ChampSelectTeamMember member = champSelect.getChampSelectUtilityContext().getSelf();
        JSONArray selection = new JSONArray().put(member.getSpell1Id()).put(member.getSpell2Id());
        int queueId = champSelect.getChampSelectSettingsContext().getQueueId();
        LCUPreferences lcuPreferences = swiftrift.getLeagueClient().getCachedValue(CacheElement.LCU_PREFERENCES);
        lcuPreferences.getChampSelectPreference().ifPresent(champSelectPreference -> {
            champSelectPreference.setSummonerSpells(queueId, selection);
            try {
                swiftrift.getLeagueClient().getLedge().getPlayerPreferences().setPreferences(
                        PreferenceType.LCU_PREFERENCES, lcuPreferences.toString()
                );
            } catch (IOException e) {
                Logger.error(e);
            }
        });
    }
}
