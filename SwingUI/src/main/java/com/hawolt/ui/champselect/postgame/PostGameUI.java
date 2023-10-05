package com.hawolt.ui.champselect.postgame;

import com.hawolt.Swiftrift;
import com.hawolt.client.resources.ledge.leagues.objects.LeagueNotification;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.ui.layout.LayoutComponent;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created: 11/09/2023 20:58
 * Author: Twitter @hawolt
 **/

public class PostGameUI extends ChildUIComponent implements ActionListener {
    private final Swiftrift swiftrift;
    private final PostGameHeader header;
    private final PostGameScoreboard scoreboard;
    private final LFlatButton close;

    public PostGameUI(Swiftrift swiftrift) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.add(header = new PostGameHeader(), BorderLayout.NORTH);
        this.add(scoreboard = new PostGameScoreboard(), BorderLayout.CENTER);
        this.add(close = new LFlatButton("Play Again", LTextAlign.CENTER, HighlightType.COMPONENT), BorderLayout.SOUTH);
        this.close.addActionListener(this);
        this.swiftrift = swiftrift;
    }

    public void build(IResponse response, List<LeagueNotification> notifications) {
        Swiftrift.debouncer.debounce("Reconnect", () -> this.swiftrift.getLayoutManager().getHeader().hide(LayoutComponent.RECONNECT), 0, TimeUnit.MILLISECONDS);
        this.build(response.asString(), notifications);
    }

    public void build(String response, List<LeagueNotification> notifications) {
        JSONObject data = new JSONObject(response);
        header.update(data, notifications);
        scoreboard.update(data);
        /*JOIN POST GAME CHAT
        String xmppRoomName = data.getString("roomName");
        String xmppRoomPassword = data.getString("roomPassword");
        PartyMucJwtDto mucJwtDto = new PartyMucJwtDto(data.getJSONObject("mucJwtDto"));
        client.getXMPPClient().joinProtectedMuc(xmppRoomName, mucJwtDto.getDomain(), xmppRoomPassword);
        */
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Swiftrift.service.execute(() -> {
            try {
                this.swiftrift.getLayoutManager().getQueue().getAvailableLobbies().forEach(lobby -> lobby.toggleButtonState(false, true));
                this.swiftrift.getLeagueClient().getLedge().getParties().ready();
                this.swiftrift.getLayoutManager()
                        .getChampSelectUI()
                        .getChampSelect()
                        .getChampSelectDataContext()
                        .getPUUIDResolver()
                        .clear();
                this.swiftrift.getHeader().selectAndShowComponent(LayoutComponent.PLAY);
                this.swiftrift.getLayoutManager().getChampSelectUI().showBlankPanel();
            } catch (IOException ex) {
                Logger.error(ex);
            }
        });
    }
}
