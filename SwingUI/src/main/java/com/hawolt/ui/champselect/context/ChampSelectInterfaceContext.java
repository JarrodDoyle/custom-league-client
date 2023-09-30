package com.hawolt.ui.champselect.context;

import com.hawolt.Swiftrift;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.generic.ChampSelectRuneComponent;

/**
 * Created: 10/09/2023 03:17
 * Author: Twitter @hawolt
 **/

public interface ChampSelectInterfaceContext {
    ChampSelectRuneComponent getRuneSelectionPanel(AbstractRenderInstance instance);

    void filterChampion(String champion);

    Swiftrift getLeagueClientUI();
}
