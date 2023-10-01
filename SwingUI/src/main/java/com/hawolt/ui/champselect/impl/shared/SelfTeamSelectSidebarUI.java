package com.hawolt.ui.champselect.impl.shared;

import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.data.ChampSelectMember;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;

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
        if (members.length != 0) {
            this.populate(context, members);
        } else {
            this.populate(context, members);
        }
        this.configureSummonerSpells(context);
        this.revalidate();
    }
}
