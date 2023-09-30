package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.async.ExecutorManager;
import com.hawolt.logger.Logger;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.data.*;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created: 30/08/2023 16:07
 * Author: Twitter @hawolt
 **/

public class ChampSelectSidebarUI extends ChampSelectUIComponent {
    private final AbstractRenderInstance instance;
    protected final Map<Integer, ChampSelectMemberElement> map = new HashMap<>();
    protected final ChildUIComponent display;

    protected final ChildUIComponent main;
    protected final ChampSelectTeam team;
    protected ChampSelectTeamType type;

    public ChampSelectSidebarUI(AbstractRenderInstance instance, ChampSelectTeam team) {
        instance.register(this);
        this.instance = instance;
        ColorPalette.addThemeListener(this);
        this.team = team;
        this.setLayout(new BorderLayout());
        this.display = new ChildUIComponent();
        this.setBackground(ColorPalette.backgroundColor);
        this.main = new ChildUIComponent(new BorderLayout());
        this.setPreferredSize(new Dimension(300, 0));
        this.display.setBackground(ColorPalette.backgroundColor);
        this.main.setBackground(ColorPalette.backgroundColor);
        this.main.add(display, BorderLayout.CENTER);
        this.add(main, BorderLayout.CENTER);
        this.display.setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    @Override
    public void init(ChampSelectContext context) {
        this.map.clear();
        this.display.removeAll();
        this.type = getChampSelectTeamType(context);
        ChampSelectMember[] members = get(context, type);
        this.display.setBackground(ColorPalette.backgroundColor);
        populate(members);
        revalidate();
    }

    protected void populate(ChampSelectMember... members) {
        Logger.debug("POPULATE CHAMP SELECT");
        this.display.setLayout(new GridLayout(Math.max(1, members.length), 0, 0, 5));
        for (ChampSelectMember member : members) {
            ExecutorService loader = ExecutorManager.getService("name-loader");
            ChampSelectMemberElement element = new ChampSelectMemberElement(instance, type, team, member);
            element.setBackground(ColorPalette.backgroundColor);
            map.put(member.getCellId(), element);
            loader.execute(element);
            this.display.add(element);
        }
        Logger.debug("CHAMP SELECT POPULATED");
    }

    @Override
    public void update(ChampSelectContext context) {
        for (ChampSelectMember member : get(context, type)) {
            if (!map.containsKey(member.getCellId())) continue;
            ChampSelectMemberElement element = map.get(member.getCellId());
            if (element == null) continue;
            element.update(context, member);
        }
    }

    protected ChampSelectMember[] get(ChampSelectContext context, ChampSelectTeamType type) {
        ChampSelectSettingsContext settingsContext = context.getChampSelectSettingsContext();
        switch (type) {
            case ALLIED -> {
                return settingsContext.getCells(ChampSelectTeamType.ALLIED, TeamMemberFunction.INSTANCE);

            }
            case ENEMY -> {
                return settingsContext.getCells(ChampSelectTeamType.ENEMY, MemberFunction.INSTANCE);
            }
        }
        return new ChampSelectMember[0];
    }

    @NotNull
    protected ChampSelectTeamType getChampSelectTeamType(ChampSelectContext context) {
        ChampSelectTeamMember self = context.getChampSelectUtilityContext().getSelf();
        int alliedTeamId = self.getTeamId();
        ChampSelectTeamType type;
        switch (team) {
            case BLUE -> type = alliedTeamId == 1 ? ChampSelectTeamType.ALLIED : ChampSelectTeamType.ENEMY;
            case PURPLE -> type = alliedTeamId == 2 ? ChampSelectTeamType.ALLIED : ChampSelectTeamType.ENEMY;
            default -> throw new IllegalStateException("Unexpected value: " + team);
        }
        return type;
    }
}
