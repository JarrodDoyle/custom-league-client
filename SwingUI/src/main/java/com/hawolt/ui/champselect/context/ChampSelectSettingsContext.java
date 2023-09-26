package com.hawolt.ui.champselect.context;

import com.hawolt.ui.champselect.data.ActionObject;
import com.hawolt.ui.champselect.data.ChampSelectTeamType;
import com.hawolt.ui.champselect.data.DraftMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Created: 10/09/2023 03:31
 * Author: Twitter @hawolt
 **/

public interface ChampSelectSettingsContext {

    <T> T getCells(ChampSelectTeamType type, Function<JSONArray, T> function);

    Map<Integer, List<ActionObject>> getActionSetMapping();

    JSONArray getCells(ChampSelectTeamType type);

    Set<String> getCells();

    int[] getChampionsAvailableForBan();

    int[] getChampionsAvailableForPick();

    void populate(JSONObject payload);

    long getGameId();

    int getQueueId();

    int getRecoveryCounter();

    String getContextId();

    int getCounter();

    String getPhaseName();

    boolean isAllowDuplicatePicks();

    boolean isSkipChampionSelect();

    boolean isAllowSkinSelection();

    boolean isAllowOptingOutOfBanning();

    long getCurrentTotalTimeMillis();

    long getCurrentTimeRemainingMillis();

    int getLocalPlayerCellId();

    int getCurrentActionSetIndex();

    String getTeamId();

    String getSubphase();

    String getTeamChatRoomId();

    long getLastUpdate();

    JSONArray getTradeArray();

    JSONArray getSwapArray();

    JSONObject getCellData();

    JSONArray getChampionBench();

    DraftMode getDraftMode();

    int[] getBannedChampions();

    int[] getSelectedChampions();
}
