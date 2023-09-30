package com.hawolt.ui.champselect.impl.blind;

import com.hawolt.async.ExecutorManager;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.data.ChampSelectMember;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.generic.impl.ChampSelectMemberElement;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;
import com.hawolt.ui.generic.themes.ColorPalette;

import java.awt.*;
import java.util.concurrent.ExecutorService;

/**
 * Created: 31/08/2023 21:10
 * Author: Twitter @hawolt
 **/

public class BlindSelectSidebarUI extends ChampSelectSidebarUI {
    private final AbstractRenderInstance instance;

    public BlindSelectSidebarUI(AbstractRenderInstance instance, ChampSelectTeam team) {
        super(instance, team);
        this.instance = instance;
    }

    @Override
    public void init(ChampSelectContext context) {
        this.map.clear();
        this.display.removeAll();
        this.type = getChampSelectTeamType(context);
        ChampSelectMember[] members = get(context, type);
        this.display.setBackground(ColorPalette.backgroundColor);
        if (members.length != 0) {
            populate(members);
        } else {
            this.display.setLayout(new GridLayout(5, 0, 0, 5));
            for (int i = 0; i < 5; i++) {
                ExecutorService loader = ExecutorManager.getService("name-loader");
                ChampSelectMemberElement element = new ChampSelectMemberElement(instance, type, team, null);
                map.put(i, element);
                loader.execute(element);
                this.display.add(element);
            }
        }
        revalidate();
    }
}
