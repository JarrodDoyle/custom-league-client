package com.hawolt.async.rms;

import com.hawolt.Swiftrift;
import com.hawolt.generic.data.Platform;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.util.other.Launcher;

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
        Launcher.launch(swiftrift.getSettingService(), platform, riotMessageServiceMessage.getPayload().getPayload());
        swiftrift.getLayoutManager().getQueue().getAvailableLobbies().forEach(
                lobby -> lobby.toggleButtonState(false, false)
        );
        swiftrift.getLayoutManager().getChampSelectUI().showBlankPanel();
        swiftrift.getChatSidebar().getEssentials().disableQueueState();
    }

}
