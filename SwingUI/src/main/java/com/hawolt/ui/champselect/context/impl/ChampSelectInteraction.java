package com.hawolt.ui.champselect.context.impl;

import com.hawolt.ui.champselect.ChampSelectUI;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectContextProvider;
import com.hawolt.ui.champselect.context.ChampSelectInteractionContext;
import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.data.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created: 10/09/2023 03:28
 * Author: Twitter @hawolt
 **/

public class ChampSelectInteraction extends ChampSelectContextProvider implements ChampSelectInteractionContext {
    public ChampSelectInteraction(ChampSelectUI champSelectUI, ChampSelectContext context) {
        super(champSelectUI, context);
    }

    private JSONArray getSwapArray() {
        return context.getChampSelectSettingsContext().getSwapArray();
    }

    private JSONArray getTradeArray() {
        return context.getChampSelectSettingsContext().getTradeArray();
    }

    private Map<Integer, List<ActionObject>> getActionSetMapping() {
        return context.getChampSelectSettingsContext().getActionSetMapping();
    }

    private List<ActionObject> getSelection(ChampSelectTeamType teamType, String type) {
        ChampSelectSettingsContext settingsContext = context.getChampSelectSettingsContext();
        List<Integer> cellIds = Arrays.stream(settingsContext.getCells(teamType, MemberFunction.INSTANCE))
                .map(ChampSelectMember::getCellId)
                .toList();
        return getActionSetMapping().values()
                .stream()
                .flatMap(Collection::stream)
                .filter(actionObject -> actionObject.getType().equals(type))
                .filter(actionObject -> cellIds.contains(actionObject.getActorCellId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ActionObject> getBanSelection(ChampSelectTeamType type) {
        return getSelection(type, "BAN");
    }

    @Override
    public List<ActionObject> getPickSelection(ChampSelectTeamType type) {
        return getSelection(type, "PICK");
    }

    @Override
    public TradeStatus[] getTrades() {
        return getTradeArray().toList()
                .stream()
                .map(o -> (HashMap<?, ?>) o)
                .map(JSONObject::new)
                .map(TradeStatus::new)
                .toArray(TradeStatus[]::new);
    }

    @Override
    public Optional<TradeStatus> getTrade(int cellId) {
        return getTradeArray().toList()
                .stream()
                .map(o -> (HashMap<?, ?>) o)
                .map(JSONObject::new)
                .map(TradeStatus::new)
                .filter(status -> status.getCellId() == cellId)
                .findFirst();
    }

    @Override
    public Optional<TradeStatus> getActiveTrade() {
        return getTradeArray().toList()
                .stream()
                .map(o -> (HashMap<?, ?>) o)
                .map(JSONObject::new)
                .map(TradeStatus::new)
                .filter(status -> "SENT".equals(status.getState()) || "RECEIVED".equals(status.getState()))
                .findFirst();
    }

    @Override
    public PickOrderStatus[] getPickSwaps() {
        return getSwapArray().toList()
                .stream()
                .map(o -> (HashMap<?, ?>) o)
                .map(JSONObject::new)
                .map(PickOrderStatus::new)
                .toArray(PickOrderStatus[]::new);
    }

    @Override
    public Optional<PickOrderStatus> getPickSwap(int cellId) {
        return getSwapArray().toList()
                .stream()
                .map(o -> (HashMap<?, ?>) o)
                .map(JSONObject::new)
                .map(PickOrderStatus::new)
                .filter(status -> status.getCellId() == cellId)
                .findFirst();
    }

    @Override
    public Optional<PickOrderStatus> getPickSwap() {
        return getSwapArray().toList()
                .stream()
                .map(o -> (HashMap<?, ?>) o)
                .map(JSONObject::new)
                .map(PickOrderStatus::new)
                .filter(status -> "SENT".equals(status.getState()) || "RECEIVED".equals(status.getState()))
                .findFirst();
    }
}
