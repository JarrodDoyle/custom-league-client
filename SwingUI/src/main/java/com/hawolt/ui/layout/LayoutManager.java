package com.hawolt.ui.layout;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.ui.champselect.ChampSelectUI;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.github.ReleaseWindow;
import com.hawolt.ui.profile.ProfileWindow;
import com.hawolt.ui.queue.QueueWindow;
import com.hawolt.ui.reconnect.ReconnectWindow;
import com.hawolt.ui.store.StoreWindow;

import java.awt.*;

/**
 * Created: 09/08/2023 15:48
 * Author: Twitter @hawolt
 **/

public class LayoutManager extends ChildUIComponent implements ILayoutManager {

    private final CardLayout layout = new CardLayout();
    private final ChildUIComponent center;
    private final QueueWindow queue;
    private final StoreWindow store;
    private final ProfileWindow profile;
    private final ChampSelectUI champSelect;
    private LayoutHeader header;

    public LayoutManager(Swiftrift swiftrift) {
        super(new BorderLayout());

        LeagueClient client = swiftrift.getLeagueClient();
        this.add(center = new ChildUIComponent(layout), BorderLayout.CENTER);
        //this.center.setBorder(new MatteBorder(2, 0, 0, 0, Color.DARK_GRAY));

        this.center.add("home", new ReleaseWindow());
        this.center.add("store", store = new StoreWindow(client));
        this.center.add("play", queue = new QueueWindow(swiftrift));
        this.center.add("reconnect", new ReconnectWindow(swiftrift));
        this.center.add("profile", profile = new ProfileWindow(swiftrift));
        this.center.add("champselect", champSelect = new ChampSelectUI(swiftrift));
        layout.show(center, "github");
    }

    public void showClientComponent(String name) {
        layout.show(center, name);
    }

    public LayoutHeader getHeader() {
        return header;
    }

    public void setHeader(LayoutHeader header) {
        this.header = header;
    }


    public ChampSelectUI getChampSelectUI() {
        return champSelect;
    }

    public QueueWindow getQueue() {
        return queue;
    }

    public ProfileWindow getProfile() {
        return profile;
    }

    public StoreWindow getStore() {
        return store;
    }

    @Override
    public void showComponent(String panel) {
        this.layout.show(center, panel);
    }
}
