package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.preferences.PlayerPreferencesLedge;
import com.hawolt.client.resources.ledge.preferences.objects.lcupreferences.LCUPreferences;
import com.hawolt.io.Core;
import com.hawolt.io.RunLevel;
import com.hawolt.logger.Logger;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.data.*;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 30/08/2023 16:07
 * Author: Twitter @hawolt
 **/

public class ChampSelectSidebarUI extends ChampSelectUIComponent {
    private final AbstractRenderInstance renderInstance;
    protected final Map<Integer, ChampSelectMemberElement> map = new HashMap<>();
    protected final ChildUIComponent display;

    protected final ChildUIComponent main;
    protected final ChampSelectTeam team;
    protected ChampSelectTeamType type;

    public ChampSelectSidebarUI(AbstractRenderInstance renderInstance, ChampSelectTeam team) {
        ColorPalette.addThemeListener(this);
        this.team = team;
        this.setLayout(new BorderLayout());
        this.renderInstance = renderInstance;
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
    public void init() {
        this.initialize(context);
        this.setup(context);
        this.configureSummonerSpells(context);
        this.revalidate();
    }

    protected void initialize(ChampSelectContext context) {
        this.map.clear();
        this.display.removeAll();
        this.type = getChampSelectTeamType(context);
        this.display.setBackground(ColorPalette.backgroundColor);
    }

    protected void setup(ChampSelectContext context) {
        ChampSelectMember[] members = get(context, type);
        populate(context, members);
    }

    protected void configureSummonerSpells(ChampSelectContext context) {
        ChampSelectTeamMember member = context.getChampSelectUtilityContext().getSelf();
        if (member.getTeamId() == team.ordinal() + 1) {
            configureSpellSelection(context);
        }
    }

    protected void populate(ChampSelectContext context, ChampSelectMember... members) {
        Swiftrift swiftrift = context.getChampSelectInterfaceContext().getLeagueClientUI();
        this.display.setLayout(new GridLayout(Math.max(1, members.length), 0, 0, 5));
        for (ChampSelectMember member : members) {
            ChampSelectMemberElement element = new ChampSelectMemberElement(swiftrift, type, team, member);
            element.setBackground(ColorPalette.backgroundColor);
            map.put(member.getCellId(), element);
            display.add(element);
        }
    }


    public void configureSpellSelection(ChampSelectContext context) {
        int targetQueueId = context.getChampSelectSettingsContext().getQueueId();
        int[] supportedQueueIds = renderInstance.getSupportedQueueIds();
        for (int supportedQueueId : supportedQueueIds) {
            if (supportedQueueId == targetQueueId) {
                LeagueClient client = context.getChampSelectDataContext().getLeagueClient();
                if (client != null) {
                    try {
                        LCUPreferences preferences = client.getCachedValueOrElse(
                                CacheElement.LCU_PREFERENCES,
                                new LCUPreferences(
                                        PlayerPreferencesLedge.convertYamlToJson(
                                                Core.read(RunLevel.get("LCUPreferences")).toString()
                                        )
                                )
                        );
                        preferences.getChampSelectPreference().ifPresentOrElse(champSelectPreference -> {
                            JSONArray preselected = champSelectPreference.getSummonerSpells(targetQueueId);
                            Logger.error(
                                    "Visually preselect {}:{}",
                                    preselected.getInt(0),
                                    preselected.getInt(1)
                            );
                            renderInstance.getGameSettingUI().preselectSummonerSpells(preselected);
                        }, () -> Logger.warn("LCUPreferences not available"));
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                } else {
                    // Simulate preference for offline test environment
                    Logger.error("NO CLIENT AVAILABLE");
                }
            }
        }
    }

    @Override
    public void update() {
        this.update(context);
    }

    protected void update(ChampSelectContext context) {
        if (type == null) return;
        for (ChampSelectMember member : get(context, type)) {
            if (!map.containsKey(member.getCellId())) continue;
            ChampSelectMemberElement element = map.get(member.getCellId());
            if (element == null) continue;
            element.handle(context, member);
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
