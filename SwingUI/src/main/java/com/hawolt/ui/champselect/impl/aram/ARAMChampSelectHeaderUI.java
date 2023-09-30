package com.hawolt.ui.champselect.impl.aram;

import com.hawolt.ui.champselect.data.ChampSelectPhase;
import com.hawolt.ui.champselect.generic.impl.ChampSelectHeaderUI;

/**
 * Created: 31/08/2023 17:17
 * Author: Twitter @hawolt
 **/

public class ARAMChampSelectHeaderUI extends ChampSelectHeaderUI {

    public ARAMChampSelectHeaderUI() {
        super();
    }

    @Override
    protected String getPhaseDescription(ChampSelectPhase phase) {
        return "PREPARE FOR BATTLE";
    }

    @Override
    protected ChampSelectPhase getChampSelectPhase() {
        return ChampSelectPhase.FINALIZE;
    }
}
