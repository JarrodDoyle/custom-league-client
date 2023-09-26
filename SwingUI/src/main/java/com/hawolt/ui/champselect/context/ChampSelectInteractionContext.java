package com.hawolt.ui.champselect.context;

import com.hawolt.ui.champselect.data.ActionObject;
import com.hawolt.ui.champselect.data.ChampSelectTeamType;
import com.hawolt.ui.champselect.data.PickOrderStatus;
import com.hawolt.ui.champselect.data.TradeStatus;

import java.util.List;
import java.util.Optional;

/**
 * Created: 10/09/2023 03:10
 * Author: Twitter @hawolt
 **/

public interface ChampSelectInteractionContext {

    List<ActionObject> getBanSelection(ChampSelectTeamType type);

    List<ActionObject> getPickSelection(ChampSelectTeamType type);

    Optional<TradeStatus> getTrade(int cellId);

    Optional<TradeStatus> getActiveTrade();

    TradeStatus[] getTrades();

    Optional<PickOrderStatus> getPickSwap(int cellId);

    Optional<PickOrderStatus> getPickSwap();

    PickOrderStatus[] getPickSwaps();
}
