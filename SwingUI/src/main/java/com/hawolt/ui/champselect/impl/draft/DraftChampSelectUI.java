package com.hawolt.ui.champselect.impl.draft;

import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.impl.ChampSelectCenterUI;
import com.hawolt.ui.champselect.generic.impl.ChampSelectHeaderUI;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;
import com.hawolt.ui.champselect.impl.MatchmadeRenderInstance;

import java.awt.event.ActionListener;

/**
 * Created: 29/08/2023 17:04
 * Author: Twitter @hawolt
 **/

public class DraftChampSelectUI extends MatchmadeRenderInstance implements ActionListener {

    public static DraftChampSelectUI INSTANCE = new DraftChampSelectUI(ChampSelectType.values());

    public DraftChampSelectUI(ChampSelectType... supportedTypes) {
        super(supportedTypes);
    }

    @Override
    protected ChampSelectCenterUI getCenterUI(AbstractRenderInstance instance, ChampSelectType... supportedTypes) {
        return new DraftCenterUI(instance, supportedTypes);
    }

    @Override
    protected ChampSelectSidebarUI getSidebarUI(ChampSelectTeam team) {
        return new DraftSelectSidebarUI(this, team);
    }

    @Override
    protected ChampSelectHeaderUI getHeaderUI() {
        return new DraftChampSelectHeaderUI();
    }

    @Override
    protected Integer[] getAllowedSummonerSpells() {
        return new Integer[]{1, 3, 4, 6, 7, 11, 12, 13, 14, 21};
    }

    @Override
    protected void stopChampSelect() {
        this.headerUI.reset();
    }

    @Override
    public int[] getSupportedQueueIds() {
        return new int[]{400, 420, 440};
    }

    @Override
    public String getCardName() {
        return "draft";
    }
}
