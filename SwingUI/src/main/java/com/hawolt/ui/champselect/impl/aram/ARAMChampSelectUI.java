package com.hawolt.ui.champselect.impl.aram;

import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.impl.ChampSelectCenterUI;
import com.hawolt.ui.champselect.generic.impl.ChampSelectHeaderUI;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;
import com.hawolt.ui.champselect.impl.MatchmadeRenderInstance;
import com.hawolt.ui.champselect.impl.shared.SelfTeamSelectSidebarUI;

import java.awt.*;

/**
 * Created: 03/09/2023 14:16
 * Author: Twitter @hawolt
 **/

public class ARAMChampSelectUI extends MatchmadeRenderInstance {
    public static ARAMChampSelectUI INSTANCE = new ARAMChampSelectUI(ChampSelectType.PICK);

    public ARAMChampSelectUI(ChampSelectType... supportedTypes) {
        super(supportedTypes);
        this.centerUI.getNorthernChild().add(new ARAMBenchUI(this), BorderLayout.NORTH);
    }

    @Override
    protected ChampSelectCenterUI getCenterUI(AbstractRenderInstance instance, ChampSelectType... supportedTypes) {
        return new ARAMCenterUI(instance);
    }

    @Override
    protected ChampSelectSidebarUI getSidebarUI(ChampSelectTeam team) {
        return new SelfTeamSelectSidebarUI(this, team);
    }

    @Override
    protected ChampSelectHeaderUI getHeaderUI() {
        return new ARAMChampSelectHeaderUI();
    }


    @Override
    protected Integer[] getAllowedSummonerSpells() {
        return new Integer[]{1, 3, 4, 6, 7, 13, 14, 21, 32};
    }

    @Override
    protected void stopChampSelect() {

    }

    @Override
    public int[] getSupportedQueueIds() {
        return new int[]{450};
    }

    @Override
    public String getCardName() {
        return "aram";
    }
}
