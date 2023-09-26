package com.hawolt.async.gsm;

import com.hawolt.client.LeagueClient;
import com.hawolt.event.EventListener;
import com.hawolt.event.impl.GameStartEvent;
import com.hawolt.logger.Logger;

/**
 * Created: 26/09/2023 18:15
 * Author: Twitter @hawolt
 **/

public class GameStartHandler implements EventListener<GameStartEvent> {
    private final LeagueClient leagueClient;

    public GameStartHandler(LeagueClient leagueClient) {
        this.leagueClient = leagueClient;
    }

    @Override
    public void onEvent(String s, GameStartEvent gameStartEvent) throws Exception {
        if (leagueClient.getLedge().getParties().enteredGame()) {
            Logger.debug("[gsl] game has been entered");
        } else {
            Logger.debug("[gsl] failed to notify game enter");
        }
    }
}
