package com.hawolt.async.gsm;

import com.hawolt.Swiftrift;
import com.hawolt.client.resources.ledge.preferences.objects.PreferenceType;
import com.hawolt.event.EventListener;
import com.hawolt.event.impl.GameStartEvent;
import com.hawolt.logger.Logger;
import com.hawolt.ui.champselect.context.impl.ChampSelect;
import com.hawolt.ui.champselect.data.ChampSelectTeamMember;
import com.hawolt.util.settings.SettingType;
import org.json.JSONArray;
import org.json.JSONObject;

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
        JSONArray selection = new JSONArray().put(member.getSpell1Id(), member.getSpell2Id());
        int queueId = champSelect.getChampSelectSettingsContext().getQueueId();
        JSONObject preference = swiftrift.getSettingService().getUserSettings().setSummonerSpellPreference(queueId, selection);
        if (preference == null) return;
        swiftrift.getLeagueClient().getLedge().getPlayerPreferences().setPreferences(PreferenceType.LCU_PREFERENCES, preference.toString());
        swiftrift.getSettingService().write(SettingType.PLAYER, "preferences", preference);
    }
}
