package com.hawolt.ui.queue;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyRole;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyType;
import com.hawolt.client.resources.ledge.teambuilder.objects.MatchContext;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.utility.Base64GZIP;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.ui.chat.friendlist.ChatSidebarEssentials;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LLabel;
import com.hawolt.ui.generic.component.LScrollPane;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.ui.queue.pop.QueueDialog;
import com.hawolt.util.audio.AudioEngine;
import com.hawolt.util.audio.Sound;
import com.hawolt.util.paint.PaintHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Created: 10/08/2023 15:52
 * Author: Twitter @hawolt
 **/

public class QueueWindow extends ChildUIComponent implements Runnable, PacketCallback, IServiceMessageListener<RiotMessageServiceMessage> {
    private final Map<Integer, String> mapping = new HashMap<>() {
        {
            //TFT
            put(1090, "NORMAL");
            put(1100, "RANKED");
            put(1130, "HYPER ROLL");
            put(1160, "DOUBLE UP");

            // TUTORIAL
            put(2000, "TUTORIAL 1");
            put(2010, "TUTORIAL 2");
            put(2020, "TUTORIAL 3");

            // ARAM
            put(450, "ARAM");

            // BOT
            put(830, "INTRO");
            put(840, "EASY");
            put(850, "INTERMEDIATE");

            // NORMAL
            put(430, "BLIND PICK");
            put(420, "RANKED SOLO/DUO");
            put(400, "DRAFT PICK");
            put(440, "RANKED FLEX");
        }
    };

    private final List<String> supportedModes = Arrays.asList("TUTORIAL", "ARAM", "BOTS", "BLIND", "DRAFT", "RANKED-FLEX", "RANKED-SOLO", "TFT");
    private final Map<String, GameLobby> relation = new HashMap<>();
    private final CardLayout layout = new CardLayout();
    private final DraftGameLobby draftGameLobby;
    private final TFTGameLobby tftGameLobby;
    private final ChildUIComponent parent;
    private final Swiftrift swiftrift;
    private String currentMode;

    public QueueWindow(Swiftrift swiftrift) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(8, 8, 8, 8));
        this.swiftrift = swiftrift;
        this.add(parent = new ChildUIComponent(layout), BorderLayout.CENTER);
        this.relation.put("draft", draftGameLobby = new DraftGameLobby(swiftrift, parent, layout, this));
        this.relation.put("tft", tftGameLobby = new TFTGameLobby(swiftrift, parent, layout, this));
        this.parent.add("draft", relation.get("draft"));
        this.parent.add("tft", relation.get("tft"));
        Swiftrift.service.execute(this);
    }

    @Override
    public void run() {
        try {
            LeagueClient client = swiftrift.getLeagueClient();
            client.getRMSClient().getHandler().addMessageServiceListener(MessageService.TEAMBUILDER, this);
            client.getRTMPClient().getMatchMakerService().getAllQueuesCompressedAsynchronous(this);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public DraftGameLobby getDraftGameLobby() {
        return draftGameLobby;
    }

    public TFTGameLobby getTftGameLobby() {
        return tftGameLobby;
    }

    private Map<String, List<JSONObject>> getQueueMapping(JSONArray array) {
        Map<String, List<JSONObject>> map = new HashMap<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            String state = object.getString("queueState");
            if ("OFF".equals(state)) continue;
            String shortName = object.getString("shortName");
            boolean supported = false;
            for (String mode : supportedModes) {
                if (shortName.contains(mode)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) continue;
            String gameMode = object.getString("gameMode");
            if (gameMode.contains("TUTORIAL")) gameMode = "TUTORIAL";
            if ("BOT".equals(object.getString("type"))) gameMode = "BOT";
            if (!map.containsKey(gameMode)) map.put(gameMode, new ArrayList<>());
            map.get(gameMode).add(object);
        }
        return map;
    }

    private void configureQueueMapping(String body) {
        JSONArray array = new JSONArray(body);
        Map<String, List<JSONObject>> map = getQueueMapping(array);
        //int count = (int) map.keySet().stream().filter(o -> !o.contains("TUTORIAL")).count();

        ChildUIComponent modes = new ChildUIComponent(new GridBagLayout());
        modes.setBackground(ColorPalette.cardColor);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;
        constraints.weightx = 1.0;

        for (String key : map.keySet()) {
            LLabel label = new LLabel(key, LTextAlign.CENTER, true);
            ChildUIComponent grid = new ChildUIComponent(new GridLayout(0, 1, 0, 4));
            grid.setBackground(ColorPalette.cardColor);
            grid.add(label);

            for (JSONObject object : map.get(key)) {
                String name = object.getString("shortName");
                if (name.contains("CLASH") || name.contains("TFT-TUTORIAL")) continue;
                String modeName = mapping.getOrDefault(object.getInt("id"), "UNKNOWN");
                LFlatButton button = new LFlatButton(modeName.isEmpty() ? name : modeName, LTextAlign.CENTER, HighlightType.COMPONENT);
                button.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
                button.setActionCommand(object.toString());
                button.addActionListener(e -> createMatchMadeLobby(e, key));
                grid.add(button);
            }
            modes.add(grid, constraints);
            constraints.insets = new Insets(16, 0, 0, 0);
            constraints.gridy += 1;
        }

        ChildUIComponent scrollContainer = new ChildUIComponent(new BorderLayout());
        scrollContainer.setBackground(ColorPalette.cardColor);
        scrollContainer.add(modes, BorderLayout.NORTH);

        LScrollPane modesPanel = new LScrollPane(scrollContainer) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Dimension dimensions = getSize();

                // TODO: Set anti-aliasing
                g.setColor(ColorPalette.cardColor);
                PaintHelper.roundedSquare((Graphics2D) g, 0, 0, dimensions.width, dimensions.height, ColorPalette.CARD_ROUNDING, true, true, true, true);
            }
        };
        modesPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        modesPanel.setBackground(ColorPalette.backgroundColor);
        modesPanel.getVerticalScrollBar().setUnitIncrement(15);
        modesPanel.setPreferredSize(new Dimension(300, 0));

        this.add(modesPanel, BorderLayout.WEST);
        revalidate();
    }

    @Override
    public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) throws Exception {
        TypedObject data = typedObject.getTypedObject("data");
        TypedObject message = data.getTypedObject("flex.messaging.messages.AcknowledgeMessage");
        configureQueueMapping(Base64GZIP.unzipBase64(message.getString("body")));
    }

    public Collection<GameLobby> getAvailableLobbies() {
        return new ArrayList<>(relation.values());
    }

    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) {
        JSONObject payload = riotMessageServiceMessage.getPayload().getPayload();
        if (payload.has("backwardsTransitionInfo")) {
            JSONObject info = payload.getJSONObject("backwardsTransitionInfo");
            if (!info.has("backwardsTransitionReason")) return;
            handleBackwardsTransitionReason(info);
            swiftrift.getChatSidebar().getEssentials().disableQueueState();
            try {
                swiftrift.getLeagueClient().getLedge().getParties().ready();
            } catch (IOException e) {
                Logger.error(e);
            }
        } else if (payload.has("phaseName")) {
            String phaseName = payload.getString("phaseName");
            ChatSidebarEssentials essentials = swiftrift.getChatSidebar().getEssentials();
            if (phaseName.equals("MATCHMAKING")) {
                JSONObject matchmakingState = payload.getJSONObject("matchmakingState");
                if (matchmakingState.has("backwardsTransitionReason")) {
                    handleBackwardsTransitionReason(matchmakingState);
                }
                long estimatedMatchmakingTimeMillis = matchmakingState.getLong("estimatedMatchmakingTimeMillis");
                if (payload.getInt("counter") != 0) return;
                essentials.toggleQueueState(System.currentTimeMillis(), estimatedMatchmakingTimeMillis);
                revalidate();
            } else if (phaseName.equals("AFK_CHECK")) {
                AudioEngine.play(Sound.QUEUE_POP);
                JSONObject afkCheckState = payload.getJSONObject("afkCheckState");
                long maxAfkMillis = afkCheckState.getLong("maxAfkMillis");
                this.swiftrift.getLeagueClient().cache(CacheElement.CHAMP_SELECT_COUNTER, payload.getInt("counter"));
                QueueDialog dialog = new QueueDialog(Frame.getFrames()[0], "Queue Notification", maxAfkMillis);
                if (dialog.showQueueDialog().getSelection() != 1) {
                    essentials.disableQueueState();
                } else {
                    try {
                        MatchContext context = swiftrift.getLeagueClient().getLedge().getTeamBuilder().indicateAfkReadiness();
                        Logger.info("Queue Accept: {}", context.getStatus());
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            } else {
                Logger.info("Ignored RMS Packet {}", riotMessageServiceMessage);
            }
        }
    }

    private void handleBackwardsTransitionReason(JSONObject info) {
        /*
         * other existing backwardsTransitionReason
         * 1.   AFK_CHECK_FAILED
         */
        switch (info.getString("backwardsTransitionReason")) {
            case "PLAYER_TIMED_OUT_ON_REQUIRED_ACTION", "PLAYER_LEFT_CHAMPION_SELECT", "PLAYER_LEFT_MATCHMAKING" -> {
                swiftrift.getLayoutManager().getChampSelectUI().showBlankPanel();
            }
        }
    }

    public void showMatchMadeLobby(String mode) {
        this.currentMode = mode;
        if (mode.equals("tft")) {
            this.layout.show(parent, "tft");
        } else {
            DraftGameLobby draftGameLobby = (DraftGameLobby) relation.get("draft");
            Swiftrift.service.execute(draftGameLobby::selectPositionPreference);
            this.layout.show(parent, "draft");
        }
    }

    public void createMatchMadeLobby(ActionEvent e, String mode) {
        JSONObject json = new JSONObject(e.getActionCommand());
        long queueId = json.getLong("id");
        long maximumParticipantListSize = json.getLong("maximumParticipantListSize");
        PartiesLedge partiesLedge = swiftrift.getLeagueClient().getLedge().getParties();
        try {
            partiesLedge.role(PartyRole.DECLINED);
            partiesLedge.gamemode(partiesLedge.getCurrentPartyId(), maximumParticipantListSize, 0, queueId);
            partiesLedge.partytype(PartyType.OPEN);
            showMatchMadeLobby(mode);
        } catch (IOException ex) {
            Logger.error(ex);
        }
    }

    public void rebase() {
        // TODO: Show blank lobby section?
    }
}
