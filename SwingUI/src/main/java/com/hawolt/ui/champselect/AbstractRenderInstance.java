package com.hawolt.ui.champselect;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheListener;
import com.hawolt.client.resources.ledge.teambuilder.objects.MatchContext;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectDataContext;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.data.GameType;
import com.hawolt.ui.champselect.generic.ChampSelectRuneComponent;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.champselect.generic.impl.ChampSelectChoice;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSelectionElement;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.xmpp.event.objects.conversation.history.impl.IncomingMessage;
import com.hawolt.xmpp.event.objects.presence.impl.JoinMucPresence;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created: 29/08/2023 18:04
 * Author: Twitter @hawolt
 **/

public abstract class AbstractRenderInstance extends ChampSelectUIComponent implements ChampSelectRenderer, ChampSelectChoice, CacheListener<MatchContext> {

    private final Map<ChampSelectType, ChampSelectSelectionElement> map = new ConcurrentHashMap<>();
    private final List<ChampSelectListener> listeners = new LinkedList<>();

    protected final ChildUIComponent component = new ChildUIComponent(new BorderLayout());
    protected final ChampSelectContext context;

    public AbstractRenderInstance(ChampSelectContext context) {
        this.setLayout(new BorderLayout());
        this.add(component, BorderLayout.CENTER);
        this.register(this);
        this.context = context;
    }

    public ChampSelectContext getContext() {
        return context;
    }

    public void register(ChampSelectListener listener) {
        this.listeners.add(listener);
    }

    public abstract void setGlobalRunePanel(ChampSelectRuneComponent selection);

    public abstract void invokeChampionFilter(String champion);

    public abstract void push(IncomingMessage incomingMessage);

    public abstract void push(JoinMucPresence presence);

    protected abstract void stopChampSelect();

    @Override
    public void init(ChampSelectContext context) {
        for (ChampSelectType type : map.keySet()) {
            ChampSelectSelectionElement element = map.get(type);
            element.setSelected(false);
            element.repaint();
        }
        map.clear();
    }

    @Override
    public void onChoice(ChampSelectSelectionElement element) {
        if (map.containsKey(element.getType())) {
            ChampSelectSelectionElement champSelectSelectionElement = map.get(element.getType());
            if (champSelectSelectionElement == null) return;
            champSelectSelectionElement.setSelected(false);
            champSelectSelectionElement.repaint();
        }
        map.put(element.getType(), element);
    }

    public void dodge(GameType type) {
        Swiftrift.service.execute(() -> {
            ChampSelectDataContext dataContext = context.getChampSelectDataContext();
            LeagueClient client = dataContext.getLeagueClient();
            LeagueRtmpClient rtmpClient = client.getRTMPClient();
            Swiftrift swiftrift = context.getChampSelectInterfaceContext().getLeagueClientUI();
            try {
                switch (type) {
                    case CLASSIC -> {
                        rtmpClient.getTeamBuilderService().quitGameV2Asynchronous(dataContext.getPacketCallback());
                    }
                    case CUSTOM -> {
                        Logger.debug("currently not supported");
                    }
                }
                swiftrift.getLayoutManager().getQueue().getAvailableLobbies().forEach(
                        lobby -> lobby.toggleButtonState(false, true)
                );
                swiftrift.getChatSidebar().getEssentials().disableQueueState();
                context.getChampSelectUtilityContext().quitChampSelect();
                revalidate();
            } catch (IOException e) {
                Logger.error("failed to quit game");
            }
        });
    }

    public void delegate(ChampSelectContext context, int initialCounter) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).execute(context, initialCounter);
        }
    }
}
