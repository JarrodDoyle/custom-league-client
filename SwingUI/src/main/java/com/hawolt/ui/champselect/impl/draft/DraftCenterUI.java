package com.hawolt.ui.champselect.impl.draft;

import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.data.ActionObject;
import com.hawolt.ui.champselect.data.ChampSelectPhase;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.impl.ChampSelectCenterUI;
import com.hawolt.ui.champselect.generic.impl.ChampSelectChoice;

import java.util.Optional;

/**
 * Created: 29/08/2023 18:57
 * Author: Twitter @hawolt
 **/

public class DraftCenterUI extends ChampSelectCenterUI {

    public DraftCenterUI(AbstractRenderInstance instance, ChampSelectType... supportedTypes) {
        super(instance, supportedTypes);
    }

    @Override
    public void update() {
        int currentActionSetIndex = context.getChampSelectSettingsContext().getCurrentActionSetIndex();
        Optional<ActionObject> optional = context.getChampSelectUtilityContext().getOwnBanPhase();
        boolean isBanComplete = optional.isPresent() && optional.get().isCompleted();
        ChampSelectPhase phase;
        if (currentActionSetIndex < 0) {
            phase = ChampSelectPhase.PLAN;
        } else if (currentActionSetIndex == 0) {
            if (!isBanComplete) {
                phase = ChampSelectPhase.BAN;
            } else {
                phase = ChampSelectPhase.PLAN;
            }
        } else {
            phase = ChampSelectPhase.PICK;
        }
        this.current = phase;
        if (name != null && name.equals("runes")) return;
        toggleCard(current.getName());
    }
}
