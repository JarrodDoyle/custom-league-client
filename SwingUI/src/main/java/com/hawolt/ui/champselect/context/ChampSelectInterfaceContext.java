package com.hawolt.ui.champselect.context;

import com.hawolt.Swiftrift;
import com.hawolt.ui.champselect.generic.ChampSelectRuneSelection;

/**
 * Created: 10/09/2023 03:17
 * Author: Twitter @hawolt
 **/

public interface ChampSelectInterfaceContext {
    ChampSelectRuneSelection getRuneSelectionPanel();

    void filterChampion(String champion);

    Swiftrift getLeagueClientUI();
}
