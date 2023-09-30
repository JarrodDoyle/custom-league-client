package com.hawolt.ui.champselect.context.impl;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.cache.JWT;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.ui.champselect.ChampSelectUI;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectContextProvider;
import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.data.ActionObject;
import com.hawolt.ui.champselect.data.ChampSelectTeamType;
import com.hawolt.ui.champselect.data.DraftMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created: 10/09/2023 03:32
 * Author: Twitter @hawolt
 **/

public class ChampSelectSettings extends ChampSelectContextProvider implements ChampSelectSettingsContext {
    private final List<Integer> F2P_QUEUE_IDS = Arrays.asList(400, 430, 830, 840, 850);
    private final Map<Integer, List<ActionObject>> actionSetMapping = new ConcurrentHashMap<>();
    protected int[] championsAvailableForBan;
    private boolean allowDuplicatePicks, skipChampionSelect, allowSkinSelection, allowOptingOutOfBanning;
    private int localPlayerCellId, currentActionSetIndex, counter, recoveryCounter, queueId;
    private long currentTotalTimeMillis, currentTimeRemainingMillis, gameId, lastUpdate;
    private String teamId, subphase, teamChatRoomId, phaseName, contextId;
    private JSONArray trades, swaps, bench, initialSpellIds;
    private JSONObject cells, inventoryDraft;

    public ChampSelectSettings(ChampSelectUI champSelectUI, ChampSelectContext context) {
        super(champSelectUI, context);
    }

    public void populate(JSONObject payload) {
        this.gameId = payload.getLong("gameId");
        this.queueId = payload.getInt("queueId");
        this.counter = payload.getInt("counter");
        this.phaseName = payload.getString("phaseName");
        this.contextId = payload.getString("contextId");
        this.recoveryCounter = payload.getInt("recoveryCounter");

        JSONObject championSelectState = payload.getJSONObject("championSelectState");
        JSONObject championBenchState = championSelectState.getJSONObject("championBenchState");
        this.currentTimeRemainingMillis = championSelectState.getLong("currentTimeRemainingMillis");
        this.allowOptingOutOfBanning = championSelectState.getBoolean("allowOptingOutOfBanning");
        this.currentTotalTimeMillis = championSelectState.getLong("currentTotalTimeMillis");
        this.currentActionSetIndex = championSelectState.getInt("currentActionSetIndex");
        this.allowDuplicatePicks = championSelectState.getBoolean("allowDuplicatePicks");
        this.allowSkinSelection = championSelectState.getBoolean("allowSkinSelection");
        this.skipChampionSelect = championSelectState.getBoolean("skipChampionSelect");
        this.inventoryDraft = championSelectState.getJSONObject("inventoryDraft");
        this.localPlayerCellId = championSelectState.getInt("localPlayerCellId");
        this.initialSpellIds = inventoryDraft.getJSONArray("initialSpellIds");
        this.teamChatRoomId = championSelectState.getString("teamChatRoomId");
        this.swaps = championSelectState.getJSONArray("pickOrderSwaps");
        this.bench = championBenchState.getJSONArray("championIds");
        this.subphase = championSelectState.getString("subphase");
        this.trades = championSelectState.getJSONArray("trades");
        this.cells = championSelectState.getJSONObject("cells");
        this.teamId = championSelectState.getString("teamId");
        this.lastUpdate = System.currentTimeMillis();
        JSONArray actionSetList = championSelectState.getJSONArray("actionSetList");
        for (int i = 0; i < actionSetList.length(); i++) {
            JSONArray actionSetListChild = actionSetList.getJSONArray(i);
            List<ActionObject> list = new ArrayList<>();
            for (int j = 0; j < actionSetListChild.length(); j++) {
                ActionObject actionObject = new ActionObject(actionSetListChild.getJSONObject(j));
                list.add(actionObject);
            }
            actionSetMapping.put(i, list);
        }
        JSONObject inventoryDraft = championSelectState.getJSONObject("inventoryDraft");
        List<String> disabledChampionIds = inventoryDraft.getJSONArray("disabledChampionIds")
                .toList()
                .stream()
                .map(Object::toString)
                .toList();
        championsAvailableForBan = inventoryDraft.getJSONArray("allChampionIds")
                .toList()
                .stream()
                .map(Object::toString)
                .filter(o -> !disabledChampionIds.contains(o))
                .mapToInt(Integer::parseInt)
                .toArray();
        champSelectUI.update(context);
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public JSONArray getTradeArray() {
        return trades;
    }

    @Override
    public JSONArray getSwapArray() {
        return swaps;
    }

    @Override
    public JSONObject getCellData() {
        return cells;
    }

    @Override
    public JSONArray getInitialSpellIds() {
        return initialSpellIds;
    }

    @Override
    public JSONObject getInventoryDraft() {
        return inventoryDraft;
    }

    @Override
    public JSONArray getChampionBench() {
        return bench;
    }

    @Override
    public DraftMode getDraftMode() {
        return switch (getActionSetMapping().size()) {
            case 0 -> DraftMode.ARAM;
            case 1 -> DraftMode.BLIND;
            default -> DraftMode.DRAFT;
        };
    }

    @Override
    public boolean isAllowDuplicatePicks() {
        return allowDuplicatePicks;
    }

    @Override
    public boolean isSkipChampionSelect() {
        return skipChampionSelect;
    }

    @Override
    public boolean isAllowSkinSelection() {
        return allowSkinSelection;
    }

    @Override
    public boolean isAllowOptingOutOfBanning() {
        return allowOptingOutOfBanning;
    }

    @Override
    public long getCurrentTotalTimeMillis() {
        return currentTotalTimeMillis;
    }

    @Override
    public long getCurrentTimeRemainingMillis() {
        return currentTimeRemainingMillis;
    }

    @Override
    public int getLocalPlayerCellId() {
        return localPlayerCellId;
    }

    @Override
    public int getCurrentActionSetIndex() {
        return currentActionSetIndex;
    }

    @Override
    public String getTeamId() {
        return teamId;
    }

    @Override
    public String getSubphase() {
        return subphase;
    }

    @Override
    public String getTeamChatRoomId() {
        return teamChatRoomId;
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public int getRecoveryCounter() {
        return recoveryCounter;
    }

    @Override
    public int getQueueId() {
        return queueId;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    @Override
    public String getPhaseName() {
        return phaseName;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    @Override
    public JSONArray getCells(ChampSelectTeamType type) {
        return cells.getJSONArray(type.getIdentifier());
    }

    @Override
    public <T> T getCells(ChampSelectTeamType type, Function<JSONArray, T> function) {
        return function.apply(getCells(type));
    }


    @Override
    public Map<Integer, List<ActionObject>> getActionSetMapping() {
        return actionSetMapping;
    }

    @Override
    public Set<String> getCells() {
        return cells.keySet();
    }

    @Override
    public int[] getChampionsAvailableForBan() {
        return championsAvailableForBan;
    }


    @Override
    public int[] getChampionsAvailableForPick() {
        LeagueClient client = context.getChampSelectDataContext().getLeagueClient();
        if (client == null) return championsAvailableForBan;
        JWT jwt = client.getCachedValue(CacheElement.INVENTORY_TOKEN);
        JSONObject b = jwt.getPayload();
        JSONObject items = b.getJSONObject("items");
        JSONArray champions = items.getJSONArray("CHAMPION");
        int[] ids = new int[champions.length()];
        for (int i = 0; i < champions.length(); i++) {
            ids[i] = champions.getInt(i);
        }
        if (F2P_QUEUE_IDS.contains(queueId)) {
            LeagueClient leagueClient = context.getChampSelectDataContext().getLeagueClient();
            int levelCap = leagueClient.getCachedValue(CacheElement.FREE_TO_PLAY_LEVEL_CAP);
            Summoner self = leagueClient.getCachedValue(CacheElement.SUMMONER);
            int[] additional = leagueClient.getCachedValue(
                    self.getLevel() >= levelCap ? CacheElement.F2P_VETERAN_PLAYER : CacheElement.F2P_NEW_PLAYER
            );
            int[] modified = Arrays.copyOf(ids, ids.length + additional.length);
            System.arraycopy(additional, 0, modified, ids.length, additional.length);
            ids = modified;
        }
        return ids;
    }

    @Override
    public int[] getBannedChampions() {
        List<ActionObject> allied = context.getChampSelectInteractionContext().getBanSelection(ChampSelectTeamType.ALLIED);
        List<ActionObject> enemy = context.getChampSelectInteractionContext().getBanSelection(ChampSelectTeamType.ENEMY);
        return Stream.of(allied, enemy)
                .flatMap(Collection::stream)
                .filter(ActionObject::isCompleted)
                .mapToInt(ActionObject::getChampionId)
                .toArray();
    }

    @Override
    public int[] getSelectedChampions() {
        List<ActionObject> allied = context.getChampSelectInteractionContext().getPickSelection(ChampSelectTeamType.ALLIED);
        List<ActionObject> enemy = context.getChampSelectInteractionContext().getPickSelection(ChampSelectTeamType.ENEMY);
        return Stream.of(allied, enemy)
                .flatMap(Collection::stream)
                .filter(ActionObject::isCompleted)
                .mapToInt(ActionObject::getChampionId)
                .toArray();
    }
}
