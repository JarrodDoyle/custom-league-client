package com.hawolt.ui.champselect;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.leagues.objects.LeagueLedgeNotifications;
import com.hawolt.client.resources.ledge.leagues.objects.LeagueNotification;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.impl.payload.RiotMessageMessagePayload;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.context.ChampSelectUtilityContext;
import com.hawolt.ui.champselect.context.impl.ChampSelect;
import com.hawolt.ui.champselect.data.ChampSelectTeamMember;
import com.hawolt.ui.champselect.impl.aram.ARAMChampSelectUI;
import com.hawolt.ui.champselect.impl.blank.BlankChampSelectUI;
import com.hawolt.ui.champselect.impl.blind.BlindChampSelectUI;
import com.hawolt.ui.champselect.impl.draft.DraftChampSelectUI;
import com.hawolt.ui.champselect.postgame.PostGameUI;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.layout.LayoutComponent;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created: 29/08/2023 16:59
 * Author: Twitter @hawolt
 **/

public class ChampSelectUI extends ChildUIComponent implements IServiceMessageListener<RiotMessageServiceMessage> {
    private final Map<Integer, AbstractRenderInstance> instances = new HashMap<>();
    private final Map<Integer, String> QUEUE_RENDERER_MAPPING = new HashMap<>();
    private final CardLayout layout = new CardLayout();
    private final JComponent main = new ChildUIComponent(layout);
    private final ChampSelect champSelect;
    private Swiftrift swiftrift;
    private LeagueClient leagueClient;
    private PostGameUI postGameUI;

    public ChampSelectUI(Swiftrift swiftrift) {
        super(new BorderLayout());
        this.add(main, BorderLayout.CENTER);
        if (swiftrift != null) {
            this.swiftrift = swiftrift;
            this.postGameUI = new PostGameUI(swiftrift);
            this.leagueClient = swiftrift.getLeagueClient();
            this.champSelect = new ChampSelect(this);
            this.leagueClient.getRMSClient().getHandler().addMessageServiceListener(MessageService.LOL_PLATFORM, this);
            this.leagueClient.getRTMPClient().addDefaultCallback(champSelect.getChampSelectDataContext().getPacketCallback());
            this.main.add("summary", postGameUI);
        } else {
            this.champSelect = new ChampSelect(this);
        }
        this.addRenderInstance(BlankChampSelectUI.INSTANCE);
        this.addRenderInstance(DraftChampSelectUI.INSTANCE);
        this.addRenderInstance(BlindChampSelectUI.INSTANCE);
        this.addRenderInstance(ARAMChampSelectUI.INSTANCE);
        this.showBlankPanel();
    }

    public ChampSelectUI() {
        this(null);
    }

    public void showPostGamePanel() {
        this.layout.show(main, "summary");
    }

    public void showBlankPanel() {
        this.layout.show(main, "blank");
    }

    public PostGameUI getPostGameUI() {
        return postGameUI;
    }

    private void addRenderInstance(AbstractRenderInstance instance) {
        if (leagueClient != null) leagueClient.register(CacheElement.MATCH_CONTEXT, instance);
        instance.setGlobalRunePanel(champSelect.getChampSelectInterfaceContext().getRuneSelectionPanel());
        int[] queueIds = instance.getSupportedQueueIds();
        for (int id : queueIds) {
            Logger.info("[champ-select] register queueId:{} as '{}'", id, instance.getCardName());
            QUEUE_RENDERER_MAPPING.put(id, instance.getCardName());
            instances.put(id, instance);
        }
        this.main.add(instance.getCardName(), instance);
    }

    public void update(ChampSelectContext context) {
        int initialCounter;
        if (leagueClient != null) {
            int counter = leagueClient.getCachedValue(CacheElement.CHAMP_SELECT_COUNTER);
            initialCounter = counter + 1;
        } else {
            // LOCAL: adjust this depending on local test data
            initialCounter = 5;
        }
        ChampSelectSettingsContext settingsContext = context.getChampSelectSettingsContext();
        if (settingsContext.getCounter() == initialCounter) {
            String card = QUEUE_RENDERER_MAPPING.getOrDefault(settingsContext.getQueueId(), "blank");
            Logger.info("[champ-select] switch to card {}", card);
            this.layout.show(main, card);
            if (swiftrift != null) {
                swiftrift.getHeader().selectAndShowComponent(LayoutComponent.CHAMPSELECT);
            }
        }
        this.configureSwiftriftFocus(context);
        int queueId = settingsContext.getQueueId();
        if (instances.containsKey(queueId)) {
            this.instances.get(queueId).delegate(context, initialCounter);
            this.repaint();
        } else {
            Logger.info("No renderer available for queueId {}", queueId);
        }
    }

    private void configureSwiftriftFocus(ChampSelectContext champSelectContext) {
        Swiftrift swiftrift = champSelectContext.getChampSelectInterfaceContext().getLeagueClientUI();
        if (swiftrift == null) return;
        ChampSelectUtilityContext utilityContext = champSelectContext.getChampSelectUtilityContext();
        ChampSelectTeamMember member = utilityContext.getSelf();
        if (member != null && (!utilityContext.isLockedIn(member) && utilityContext.isPicking(member))) {
            swiftrift.focus();
        }
    }

    public ChampSelect getChampSelect() {
        return champSelect;
    }

    public LeagueClient getLeagueClient() {
        return leagueClient;
    }

    public Swiftrift getLeagueClientUI() {
        return swiftrift;
    }

    public AbstractRenderInstance getInstance(int queueId) {
        return instances.get(queueId);
    }

    @Override
    public void onMessage(RiotMessageServiceMessage unspecified) throws Exception {
        RiotMessageMessagePayload base = unspecified.getPayload();
        if (!base.getResource().endsWith("lol-platform/v1/gsm/stats")) return;
        JSONObject payload = base.getPayload();
        long gameId = payload.getLong("gameId");
        LeagueLedgeNotifications ledgeNotifications = leagueClient.getLedge().getLeague().getNotifications();
        List<LeagueNotification> leagueNotifications = ledgeNotifications.getLeagueNotifications();
        IResponse response = leagueClient.getLedge().getUnclassified().getEndOfGame(gameId);
        postGameUI.build(response, leagueNotifications);
        ChampSelectUI.this.showPostGamePanel();
        ChampSelectUI.this.swiftrift.getHeader().selectAndShowComponent(LayoutComponent.CHAMPSELECT);
        boolean processed = leagueClient.getLedge().getChallenge().notify(gameId);
        if (!processed) {
            Logger.error("unable to submit game {} as processed", gameId);
        } else {
            Logger.info("submitting game {} as processed", gameId);
        }
    }
}
