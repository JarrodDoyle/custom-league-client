package com.hawolt.async.gsm;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.gsm.GameServiceMessageLedge;
import com.hawolt.generic.data.Platform;
import com.hawolt.logger.Logger;
import com.hawolt.util.other.Launcher;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 22/08/2023 16:53
 * Author: Twitter @hawolt
 **/

public class ActiveGameInformation implements Runnable {
    private final GameServiceMessageLedge ledge;
    private final Swiftrift swiftrift;
    private final Platform platform;

    public ActiveGameInformation(Swiftrift swiftrift) {
        this.swiftrift = swiftrift;
        LeagueClient client = swiftrift.getLeagueClient();
        this.ledge = client.getLedge().getGameServiceMessage();
        this.platform = client.getPlayerPlatform();
    }

    @Override
    public void run() {
        try {
            JSONObject info = ledge.getCurrentGameInformation();
            if (!info.has("game")) return;
            JSONObject game = info.getJSONObject("game");
            if (!game.has("gameState") || game.isNull("gameState")) return;
            if (!"IN_PROGRESS".equals(game.getString("gameState"))) return;
            JSONObject credentials = info.getJSONObject("playerCredentials");
            Launcher.launch(swiftrift.getSettingService(), platform, credentials);
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}
