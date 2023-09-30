package com.hawolt.ui.champselect.impl.blind;

import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectUtilityContext;
import com.hawolt.ui.champselect.data.ActionObject;
import com.hawolt.ui.champselect.data.ChampSelectPhase;
import com.hawolt.ui.champselect.generic.impl.ChampSelectHeaderUI;

import java.util.Optional;

/**
 * Created: 31/08/2023 17:17
 * Author: Twitter @hawolt
 **/

public class BlindChampSelectHeaderUI extends ChampSelectHeaderUI {

    public BlindChampSelectHeaderUI() {
        super();
    }

    @Override
    protected String getPhaseDescription(ChampSelectPhase phase) {
        return switch (phase) {
            case IDLE -> "WAITING FOR OTHERS";
            case PICK -> "SELECT YOUR CHAMPION";
            case FINALIZE -> "PREPARE FOR BATTLE";
            default -> "UNKNOWN STATE";
        };
    }

    @Override
    protected ChampSelectPhase getChampSelectPhase(ChampSelectContext context) {
        ChampSelectUtilityContext utilityContext = context.getChampSelectUtilityContext();
        if (utilityContext.isFinalizing()) {
            return ChampSelectPhase.FINALIZE;
        } else {
            ChampSelectPhase phase;
            Optional<ActionObject> optional = utilityContext.getOwnPickPhase();
            if (optional.isPresent()) {
                ActionObject object = optional.get();
                phase = object.isCompleted() ? ChampSelectPhase.IDLE : ChampSelectPhase.PICK;
            } else {
                phase = ChampSelectPhase.PICK;
            }
            return phase;
        }
    }
}
