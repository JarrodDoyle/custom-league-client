package com.hawolt.ui.champselect;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.utility.Base64GZIP;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.ui.champselect.header.ChampSelectHeaderUI;
import com.hawolt.ui.champselect.phase.ChampSelectPhaseUI;
import com.hawolt.ui.champselect.sidebar.ChampSelectSidebarUI;
import com.hawolt.util.AudioEngine;
import com.hawolt.util.panel.ChildUIComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 06/08/2023 13:39
 * Author: Twitter @hawolt
 **/

public class ChampSelect extends ChildUIComponent implements PacketCallback, IChampSelection, ActionListener, ISpellChangedListener {

    private final ChampSelectSidebarUI teamOneUI, teamTwoUI;
    private final ChampSelectHeaderUI headerUI;
    private final ChampSelectPhaseUI phaseUI;

    private LeagueRtmpClient rtmpClient;
    private LeagueClient leagueClient;

    public ChampSelect() {
        super(new BorderLayout());
        this.add(phaseUI = new ChampSelectPhaseUI(null, this), BorderLayout.CENTER);
        this.add(headerUI = new ChampSelectHeaderUI(), BorderLayout.NORTH);
        this.add(teamOneUI = new ChampSelectSidebarUI(), BorderLayout.WEST);
        this.add(teamTwoUI = new ChampSelectSidebarUI(), BorderLayout.EAST);
        this.phaseUI.getButton().addActionListener(this);
    }

    public ChampSelect(LeagueClient client) {
        super(new BorderLayout());
        this.leagueClient = client;
        this.add(phaseUI = new ChampSelectPhaseUI(this, this), BorderLayout.CENTER);
        this.add(headerUI = new ChampSelectHeaderUI(), BorderLayout.NORTH);
        this.add(teamOneUI = new ChampSelectSidebarUI(), BorderLayout.WEST);
        this.add(teamTwoUI = new ChampSelectSidebarUI(), BorderLayout.EAST);
        this.phaseUI.getPickPhaseUI().getButton().addActionListener(this);
        this.phaseUI.getBanPhaseUI().getButton().addActionListener(this);
        this.phaseUI.getButton().addActionListener(this);
        this.rtmpClient = client.getRTMPClient();
        this.rtmpClient.setDefaultCallback(this);

        /* TODO
        Logger.error(leagueClient.getLedge().getPerks().setRunesToMakeLittleTimmyAngry());
        */
    }

    public LeagueClient getLeagueClient() {
        return leagueClient;
    }

    private Map<String, Integer> actions = new HashMap<>();
    private int currentActionSetIndex, ownTeamId;

    private ChampSelectSidebarUI getOwnSidebarUI() {
        return ownTeamId == 1 ? teamOneUI : teamTwoUI;
    }

    public String getHiddenName(String puuid) {
        ChampSelectSidebarUI sidebarUI = getOwnSidebarUI();
        if (sidebarUI == null) return null;
        return getOwnSidebarUI().find(puuid);
    }

    public void populate(JSONObject object) {
        AudioEngine.play("ChmpSlct_AChampionApproaches.wav");

        this.phaseUI.getChatUI().build();
        this.phaseUI.show("pick");
        this.resetChampSelectState();

        JSONObject state = object.getJSONObject("championSelectState");
        this.currentActionSetIndex = state.getInt("currentActionSetIndex");

        int localPlayerCellId = state.getInt("localPlayerCellId");
        JSONArray array = state.getJSONArray("actionSetList");
        for (int i = 0; i < array.length(); i++) {
            JSONArray nested = array.getJSONArray(i);
            for (int j = 0; j < nested.length(); j++) {
                JSONObject action = nested.getJSONObject(j);
                int actorCellId = action.getInt("actorCellId");
                if (actorCellId != localPlayerCellId) continue;
                String type = action.getString("type");
                int actionId = action.getInt("actionId");
                actions.put(type, actionId);
            }
        }

        JSONObject inventoryDraft = state.getJSONObject("inventoryDraft");
        JSONArray allChampionIds = inventoryDraft.getJSONArray("allChampionIds");
        int currentActionSetIndex = state.getInt("currentActionSetIndex");

        headerUI.getTimerUI().update(currentActionSetIndex, "");
        phaseUI.getBanPhaseUI().getSelectionUI().update(allChampionIds);

        JSONObject cells = state.getJSONObject("cells");
        JSONArray allied = cells.getJSONArray("alliedTeam");
        JSONArray enemy = cells.getJSONArray("enemyTeam");
        this.ownTeamId = allied.getJSONObject(0).getInt("teamId");

        JSONArray teamOne = ownTeamId == 1 ? allied : enemy;
        JSONArray teamTwo = ownTeamId == 1 ? enemy : allied;

        headerUI.getTeamTwoUI().rebuild(teamTwo);
        teamOneUI.rebuild(teamOne, localPlayerCellId);
        headerUI.getTeamOneUI().rebuild(teamOne);
        teamTwoUI.rebuild(teamTwo, localPlayerCellId);
    }

    public void update(String data) {
        try {
            //JUST LOG, GOD HAVE MERCY ON US
            Logger.info(data);

            JSONObject object = new JSONObject(data);
            if (object.getInt("counter") == 2) populate(object);
            JSONObject state = object.getJSONObject("championSelectState");
            JSONArray array = state.getJSONArray("actionSetList");
            //DISPLAY BANS
            headerUI.update(array.getJSONArray(0));

            String subphase = state.getString("subphase");
            int currentActionSetIndex = state.getInt("currentActionSetIndex");
            Logger.info("{}:{}", currentActionSetIndex, subphase);
            //DISPLAY CORRECT PHASE
            if (currentActionSetIndex == 0 && !phaseUI.getCurrent().equals("ban")) {
                phaseUI.show("ban");
            } else if (currentActionSetIndex > 0 && !phaseUI.getCurrent().equals("pick")) {
                phaseUI.show("pick");
            }
            //UPDATE HEADERS
            long currentTotalTimeMillis = state.getLong("currentTotalTimeMillis");
            long currentTimeRemainingMillis = state.getLong("currentTimeRemainingMillis");
            headerUI.getTimerUI().update(currentTotalTimeMillis, currentTimeRemainingMillis);
            headerUI.getTimerUI().update(currentActionSetIndex, subphase);
            for (int i = 1; i < array.length(); i++) {
                JSONArray phase = array.getJSONArray(i);
                teamOneUI.update(currentActionSetIndex, i, phase);
                teamTwoUI.update(currentActionSetIndex, i, phase);
            }
            //UPDATE SIDEBARS
            JSONObject cells = state.getJSONObject("cells");
            JSONArray allied = cells.getJSONArray("alliedTeam");
            for (int i = 0; i < allied.length(); i++) {
                JSONObject member = allied.getJSONObject(i);
                int teamId = member.getInt("teamId");
                ChampSelectSidebarUI champSelectSidebarUI = teamId == 1 ? teamOneUI : teamTwoUI;
                champSelectSidebarUI.update(new AlliedMember(member));
            }
            //RELOAD
            this.revalidate();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public void reconfigure() {
        if (leagueClient == null) return;
        String jwt = leagueClient.getCachedValue(CacheType.INVENTORY_TOKEN);
        JSONObject b = new JSONObject(new String(Base64.getDecoder().decode(jwt.split("\\.")[1])));
        JSONObject items = b.getJSONObject("items");
        JSONArray champions = items.getJSONArray("CHAMPION");
        Integer[] ids = new Integer[champions.length()];
        for (int i = 0; i < champions.length(); i++) {
            ids[i] = champions.getInt(i);
        }
        phaseUI.getPickPhaseUI().getSelectionUI().update(ids);
    }

    public void resetChampSelectState() {
        this.teamOneUI.reset();
        this.teamTwoUI.reset();
        this.headerUI.reset();
        this.reconfigure();
        this.revalidate();
    }

    @Override
    public void onSelect(ChampSelectPhase phase, long championId) {
        Logger.info("HOVER {}:{}:{}", phase, championId, actions.getOrDefault(phase.name(), -1));
        LeagueClientUI.service.execute(() -> {
            try {
                TypedObject object = rtmpClient.getTeamBuilderService().updateActionV1Blocking(actions.get(phase.name()), (int) championId, false);
                Logger.error(object);
            } catch (Exception e) {
                Logger.error(e);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String command = e.getActionCommand();
        LeagueClientUI.service.execute(() -> {
            try {
                switch (command) {
                    case "DODGE" -> {
                        this.rtmpClient.getTeamBuilderService().quitGameV2Asynchronous(this);
                        this.resetChampSelectState();
                        this.revalidate();
                    }
                    case "BAN" -> {
                        int championId = (int) this.phaseUI.getBanPhaseUI().getSelectionUI().getSelectedChampionId();
                        this.rtmpClient.getTeamBuilderService().updateActionV1Blocking(
                                actions.get(command),
                                championId,
                                true
                        );
                    }
                    case "PICK" -> {
                        int championId = (int) this.phaseUI.getPickPhaseUI().getSelectionUI().getSelectedChampionId();
                        this.rtmpClient.getTeamBuilderService().updateActionV1Blocking(
                                actions.get(command),
                                championId,
                                true
                        );
                    }
                }
            } catch (Exception exception) {
                Logger.error(exception);
            }
        });
    }

    @Override
    public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) {
        try {
            Logger.error(typedObject);
            if (typedObject == null || !typedObject.containsKey("data")) return;
            TypedObject data = typedObject.getTypedObject("data");
            if (data == null || !data.containsKey("body")) return;
            TypedObject body = data.getTypedObject("body");
            if (body == null) return;
            if (!body.containsKey("payload")) return;
            try {
                Object object = body.get("payload");
                if (object == null) return;
                update(Base64GZIP.unzipBase64(object.toString()));
            } catch (IOException e) {
                Logger.error(e);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    @Override
    public void onSpellSelection(int spell1Id, int spell2Id) {
        LeagueClientUI.service.execute(() -> {
            try {
                rtmpClient.getTeamBuilderService().selectSpellsAsynchronous(spell1Id, spell2Id, this);
            } catch (IOException e) {
                Logger.error(e);
            }
        });
    }
}
