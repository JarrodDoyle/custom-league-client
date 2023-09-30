package com.hawolt.ui.champselect.impl.draft;

import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.data.ActionObject;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.generic.impl.ChampSelectBanElement;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

/**
 * Created: 31/08/2023 21:10
 * Author: Twitter @hawolt
 **/

public class DraftSelectSidebarUI extends ChampSelectSidebarUI {

    private final ChampSelectBanElement[] elements = new ChampSelectBanElement[5];

    public DraftSelectSidebarUI(AbstractRenderInstance instance, ChampSelectTeam team) {
        super(instance, team);
        ChildUIComponent bans = new ChildUIComponent(new GridLayout(0, 5, 5, 0));
        bans.setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
        bans.setPreferredSize(new Dimension(0, 60));
        for (int i = 0; i < elements.length; i++) {
            ChampSelectBanElement element = new ChampSelectBanElement(new Dimension(48, 48));
            element.update(-1);
            elements[i] = element;
            bans.add(element);
        }
        this.main.add(bans, BorderLayout.NORTH);
    }

    @Override
    public void init(ChampSelectContext context) {
        super.init(context);
        for (ChampSelectBanElement element : elements) {
            element.reset();
        }
    }

    @Override
    public void update(ChampSelectContext context) {
        super.update(context);
        List<ActionObject> list = context.getChampSelectInteractionContext().getBanSelection(type);
        for (ActionObject object : list) {
            int normalizedActorCellId = object.getActorCellId() % 5;
            elements[normalizedActorCellId].update(object);
        }
    }
}
