package com.hawolt.ui.champselect.generic;

import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.rtmp.RtmpClient;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.ChampSelectListener;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.generic.utility.ChildUIComponent;

/**
 * Created: 29/08/2023 17:13
 * Author: Twitter @hawolt
 **/

public abstract class ChampSelectUIComponent extends ChildUIComponent implements ChampSelectListener {

    @Override
    public void execute(ChampSelectContext context, int initialCounter) {
        if (context == null) return;
        if (context.getChampSelectSettingsContext().getCounter() == initialCounter) {
            this.init(context);
        }
        this.update(context);
    }

    public void update(ChampSelectContext context) {
    }

    public void init(ChampSelectContext context) {
    }
}
