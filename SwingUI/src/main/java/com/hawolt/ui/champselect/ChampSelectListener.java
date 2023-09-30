package com.hawolt.ui.champselect;

import com.hawolt.ui.champselect.context.ChampSelectContext;

/**
 * Created: 30/09/2023 17:02
 * Author: Twitter @hawolt
 **/

public interface ChampSelectListener {
    void execute(ChampSelectContext context, int initialCounter);
}
