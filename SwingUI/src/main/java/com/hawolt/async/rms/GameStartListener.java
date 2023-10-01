package com.hawolt.async.rms;

import com.hawolt.Swiftrift;
import com.hawolt.async.gsm.GameClosedHandler;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.generic.data.Platform;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.util.other.Launcher;
import org.json.JSONObject;

/**
 * Created: 11/08/2023 18:01
 * Author: Twitter @hawolt
 **/

public class GameStartListener implements IServiceMessageListener<RiotMessageServiceMessage> {
    private final Swiftrift swiftrift;
    private final Platform platform;

    public GameStartListener(Swiftrift swiftrift) {
        this.platform = swiftrift.getLeagueClient().getPlayerPlatform();
        this.swiftrift = swiftrift;
    }

    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) {
        boolean gameStart = riotMessageServiceMessage.getPayload().getResource().endsWith("player-credentials-update");
        if (!gameStart) return;
        JSONObject credentials = riotMessageServiceMessage.getPayload().getPayload();
        swiftrift.getLeagueClient().cache(CacheElement.GAME_CREDENTIALS, credentials);
        Launcher.launch(swiftrift.getSettingService(), platform, credentials);
        swiftrift.getLayoutManager().getQueue().getAvailableLobbies().forEach(
                lobby -> lobby.toggleButtonState(false, false)
        );
        swiftrift.getLayoutManager().getChampSelectUI().showBlankPanel();
        swiftrift.getChatSidebar().getEssentials().disableQueueState();
    }

}
