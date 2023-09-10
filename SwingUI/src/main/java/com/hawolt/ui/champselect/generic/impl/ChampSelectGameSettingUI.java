package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.client.resources.communitydragon.spell.Spell;
import com.hawolt.client.resources.communitydragon.spell.SpellIndex;
import com.hawolt.client.resources.communitydragon.spell.SpellSource;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.custom.LHintTextField;
import com.hawolt.ui.impl.Debouncer;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.util.ui.LComboBox;
import com.hawolt.util.ui.LFlatButton;
import com.hawolt.util.ui.LHighlightType;
import com.hawolt.util.ui.LTextAlign;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created: 31/08/2023 17:41
 * Author: Twitter @hawolt
 **/

public class ChampSelectGameSettingUI extends ChampSelectUIComponent {
    private final Debouncer debouncer = new Debouncer();
    private final LComboBox<Spell> spellOne, spellTwo;
    private final LFlatButton submit, runes, dodge;

    public ChampSelectGameSettingUI(Integer... allowedSpellIds) {
        this.setLayout(new BorderLayout());
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.BLACK));
        //TODO find a data source for this
        List<Integer> temporaryWhiteList = Arrays.asList(allowedSpellIds);
        SpellIndex spellIndex = SpellSource.SPELL_SOURCE_INSTANCE.get();
        Spell[] spells = spellIndex.getAvailableSpells();
        List<Spell> list = new ArrayList<>();
        for (Spell spell : spells) {
            if (!temporaryWhiteList.contains(spell.getId())) continue;
            list.add(spell);
        }
        Spell[] allowed = list.toArray(Spell[]::new);
        ChildUIComponent spellUI = new ChildUIComponent(new GridLayout(0, 2, 5, 0));
        spellUI.add(spellOne = new LComboBox<>(allowed));
        spellUI.add(spellTwo = new LComboBox<>(allowed));
        spellUI.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(spellUI, BorderLayout.EAST);
        ChildUIComponent buttonUI = new ChildUIComponent(new GridLayout(0, 4, 5, 0));
        buttonUI.add(dodge = new LFlatButton("Dodge", LTextAlign.CENTER, LHighlightType.COMPONENT));
        buttonUI.add(submit = new LFlatButton("Submit Choice", LTextAlign.CENTER, LHighlightType.COMPONENT));
        buttonUI.add(runes = new LFlatButton("Rune Page", LTextAlign.CENTER, LHighlightType.COMPONENT));
        LHintTextField filter = new LHintTextField("Search...");
        filter.getDocument().addDocumentListener(new DocumentListener() {
            private void forward(String text) {
                debouncer.debounce(
                        "filter",
                        () -> context.getChampSelectInterfaceContext().filterChampion(text),
                        200L,
                        TimeUnit.MILLISECONDS
                );
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                forward(filter.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                forward(filter.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                forward(filter.getText());
            }
        });
        buttonUI.add(filter);
        add(buttonUI, BorderLayout.WEST);
    }

    public JComboBox<Spell> getSpellOne() {
        return spellOne;
    }

    public Spell getSelectedSpellOne() {
        return spellOne.getItemAt(spellOne.getSelectedIndex());
    }

    public JComboBox<Spell> getSpellTwo() {
        return spellTwo;
    }

    public Spell getSelectedSpellTwo() {
        return spellTwo.getItemAt(spellTwo.getSelectedIndex());
    }

    public LFlatButton getSubmitButton() {
        return submit;
    }

    public LFlatButton getRuneButton() {
        return runes;
    }

    public LFlatButton getDodgeButton() {
        return dodge;
    }
}
