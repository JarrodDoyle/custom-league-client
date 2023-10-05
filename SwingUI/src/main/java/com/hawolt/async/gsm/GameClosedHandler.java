package com.hawolt.async.gsm;

import com.hawolt.LiveGameClient;
import com.hawolt.Swiftrift;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.gsm.GameServiceMessageLedge;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.event.Event;
import com.hawolt.event.EventListener;
import com.hawolt.event.custom.LocalPlayerDeadEvent;
import com.hawolt.event.custom.StartupEvent;
import com.hawolt.event.impl.ChampionKillEvent;
import com.hawolt.event.impl.GameEndEvent;
import com.hawolt.event.impl.GameStartEvent;
import com.hawolt.logger.Logger;
import com.hawolt.ui.layout.LayoutComponent;
import com.hawolt.util.os.process.observer.ProcessCallback;
import com.hawolt.util.os.utility.SystemUtility;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GameClosedHandler implements ProcessCallback, EventListener<Event> {
    private final LiveGameClient liveGameClient;
    private final Swiftrift swiftrift;
    private final Summoner summoner;
    private boolean dead;
    private String mode;


    public GameClosedHandler(LiveGameClient liveGameClient, Swiftrift swiftrift) {
        SystemUtility.listen("League of Legends.exe", this);
        this.summoner = swiftrift.getLeagueClient().getCachedValue(CacheElement.SUMMONER);
        this.liveGameClient = liveGameClient;
        this.swiftrift = swiftrift;
    }

    @Override
    public void onEvent(String name, Event event) {
        if (event instanceof ChampionKillEvent championKillEvent) {
            this.dead = championKillEvent.getVictimName().equals(summoner.getName()) && mode.equals("TFT");
        } else if (event instanceof GameStartEvent gameStartEvent) {
            this.mode = gameStartEvent.getGameData().getGameMode();
            this.dead = false;
        } else if (event instanceof StartupEvent) {
            liveGameClient.getAllPlayers().ifPresent(allPlayers -> this.dead = allPlayers.getCurrentPlayer().IsDead());
        } else if (event instanceof LocalPlayerDeadEvent) {
            this.dead = mode.equals("TFT");
        } else if (event instanceof GameEndEvent) {
            this.allowLobbyUsage();
        }
    }

    @Override
    public void onStateChange(State state) {
        if (state != ProcessCallback.State.TERMINATED) return;
        this.swiftrift.getPresence().setIdlePresence();
        try {
            GameServiceMessageLedge ledge = swiftrift.getLeagueClient().getLedge().getGameServiceMessage();
            if (dead) {
                Logger.info("TFT-DELETE: {}", ledge.deleteGame());
            } else {
                JSONObject info = swiftrift.getLeagueClient().getLedge()
                        .getGameServiceMessage()
                        .getCurrentGameInformation();
                if (!info.has("game")) return;
                JSONObject game = info.getJSONObject("game");
                if (!game.has("gameState") || game.isNull("gameState")) return;
                if (!"IN_PROGRESS".equals(game.getString("gameState"))) return;
                //WE ARE STILL IN GAME
                Swiftrift.debouncer.debounce("Reconnect", () -> {
                    swiftrift.getLayoutManager().getHeader().selectAndShowComponent(LayoutComponent.RECONNECT);
                    swiftrift.getLayoutManager().getHeader().reveal(LayoutComponent.RECONNECT);
                }, 1, TimeUnit.SECONDS);
            }
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public void allowLobbyUsage() {
        this.swiftrift.getLayoutManager()
                .getQueue()
                .getAvailableLobbies()
                .forEach(lobby -> lobby.toggleButtonState(false, true));
    }
}
