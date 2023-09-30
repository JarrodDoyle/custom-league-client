package com.hawolt.ui.champselect.impl.blind;

import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.impl.ChampSelectCenterUI;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;
import com.hawolt.ui.champselect.impl.MatchmadeRenderInstance;

/**
 * Created: 03/09/2023 14:16
 * Author: Twitter @hawolt
 **/

public class BlindChampSelectUI extends MatchmadeRenderInstance {
    public BlindChampSelectUI(ChampSelectContext context, ChampSelectType... supportedTypes) {
        super(context, supportedTypes);
    }

    @Override
    protected ChampSelectCenterUI getCenterUI(AbstractRenderInstance instance, ChampSelectType... supportedTypes) {
        return new BlindCenterUI(instance, supportedTypes);
    }

    @Override
    protected ChampSelectSidebarUI getSidebarUI(AbstractRenderInstance instance, ChampSelectTeam team) {
        return new BlindSelectSidebarUI(instance, team);
    }

    @Override
    protected BlindChampSelectHeaderUI getHeaderUI() {
        return new BlindChampSelectHeaderUI();
    }

    @Override
    protected Integer[] getAllowedSummonerSpells() {
        return new Integer[]{1, 3, 4, 6, 7, 11, 12, 13, 14, 21};
    }

    @Override
    protected void stopChampSelect() {

    }

    @Override
    public int[] getSupportedQueueIds() {
        return new int[]{430, 830, 840, 850};
    }

    @Override
    public String getCardName() {
        return "blind";
    }
}
