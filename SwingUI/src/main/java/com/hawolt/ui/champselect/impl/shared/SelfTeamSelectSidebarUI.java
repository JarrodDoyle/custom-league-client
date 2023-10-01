package com.hawolt.ui.champselect.impl.shared;

import com.hawolt.Swiftrift;
import com.hawolt.logger.Logger;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.data.ChampSelectMember;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.generic.impl.ChampSelectMemberElement;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;
import com.hawolt.ui.generic.themes.ColorPalette;

import java.awt.*;

/**
 * Created: 31/08/2023 21:10
 * Author: Twitter @hawolt
 **/

public class SelfTeamSelectSidebarUI extends ChampSelectSidebarUI {

    public SelfTeamSelectSidebarUI(AbstractRenderInstance renderInstance, ChampSelectTeam team) {
        super(renderInstance, team);
    }

    @Override
    public void init() {
        this.initialize(context);
        ChampSelectMember[] members = get(context, type);
        Logger.info("[cs-member] {} > {}", team, members.length);
        for (ChampSelectMember member : members) {
            Logger.info("[cs-member] {} > {}", team, member);
        }
        if (members.length != 0) {
            this.populate(context, members);
        } else {
            Swiftrift swiftrift = context.getChampSelectInterfaceContext().getLeagueClientUI();
            this.display.setLayout(new GridLayout(5, 0, 0, 5));
            for (int i = 0; i < 5; i++) {
                ChampSelectMemberElement element = new ChampSelectMemberElement(swiftrift, type, team, null);
                element.setBackground(ColorPalette.backgroundColor);
                map.put(i, element);
                this.display.add(element);
            }
        }
        this.configureSummonerSpells(context);
        this.revalidate();
    }
}
