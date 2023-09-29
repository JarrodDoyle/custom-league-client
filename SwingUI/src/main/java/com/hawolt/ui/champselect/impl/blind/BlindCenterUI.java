package com.hawolt.ui.champselect.impl.blind;

import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.data.ChampSelectPhase;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.impl.ChampSelectCenterUI;

/**
 * Created: 29/08/2023 18:57
 * Author: Twitter @hawolt
 **/

public class BlindCenterUI extends ChampSelectCenterUI {

    public BlindCenterUI(AbstractRenderInstance instance, ChampSelectType... supportedTypes) {
        super(instance, supportedTypes);
    }

    @Override
    public void update() {
        this.current = ChampSelectPhase.PLAN;
        if (name != null && name.equals("runes")) return;
        toggleCard(current.getName());
    }
}
