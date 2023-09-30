package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.Swiftrift;
import com.hawolt.logger.Logger;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.data.ChampSelectPhase;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.ChampSelectRuneComponent;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 29/08/2023 18:57
 * Author: Twitter @hawolt
 **/

public abstract class ChampSelectCenterUI extends ChampSelectUIComponent {
    protected final Map<String, ChampSelectSelectionUI> map = new HashMap<>();
    protected final CardLayout layout = new CardLayout();
    protected final ChildUIComponent main, northernChild, southernChild, cards;
    private final AbstractRenderInstance renderInstance;
    protected ChampSelectRuneComponent runeSelection;
    protected ChampSelectPhase current;
    protected String name;

    public ChampSelectCenterUI(AbstractRenderInstance renderInstance, ChampSelectType... supportedTypes) {
        this.setLayout(new BorderLayout());
        this.renderInstance = renderInstance;
        this.add(main = new ChildUIComponent(new BorderLayout()), BorderLayout.CENTER);
        this.main.add(northernChild = new ChildUIComponent(new BorderLayout()), BorderLayout.NORTH);
        this.main.add(cards = new ChildUIComponent(layout), BorderLayout.CENTER);
        this.main.add(southernChild = new ChildUIComponent(new BorderLayout()), BorderLayout.SOUTH);
        for (ChampSelectType type : supportedTypes) {
            ChampSelectSelectionUI selectionUI = new ChampSelectSelectionUI(type, renderInstance);
            this.cards.add(type.getName(), selectionUI);
            this.map.put(type.getName(), selectionUI);
        }
    }

    @Override
    public void init(ChampSelectContext context) {
        int targetQueueId = context.getChampSelectSettingsContext().getQueueId();
        int[] supportedQueueIds = renderInstance.getSupportedQueueIds();
        for (int supportedQueueId : supportedQueueIds) {
            if (supportedQueueId == targetQueueId) {
                Swiftrift.service.execute(() -> runeSelection.setRuneSelection());
            }
        }
    }

    public boolean isConfigured(ChampSelectType type) {
        return map.containsKey(type.getName());
    }

    public ChampSelectSelectionUI getSelectionUI(String name) {
        return map.get(name);
    }

    public ChampSelectSelectionUI getSelectionUI(ChampSelectType type) {
        return getSelectionUI(type.getName());
    }

    public ChampSelectPhase getCurrent() {
        return current;
    }

    public ChildUIComponent getMain() {
        return main;
    }

    public ChildUIComponent getNorthernChild() {
        return northernChild;
    }

    public ChildUIComponent getSouthernChild() {
        return southernChild;
    }

    public void toggleCard(String name) {
        layout.show(cards, this.name = name);
    }

    public void toggleCurrentPhase() {
        this.toggleCard(current.getName());
        this.revalidate();
    }

    public void setRuneSelection(String name, ChampSelectRuneComponent runeSelection) {
        Logger.info("{} setting up rune panel", getClass().getSimpleName());
        this.runeSelection = runeSelection;
        this.runeSelection.getCloseButton().addActionListener(listener -> toggleCurrentPhase());
        this.cards.add(name, runeSelection);
    }
}
