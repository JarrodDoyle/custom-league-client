package com.hawolt.ui.generic.component;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LCheckBox extends JCheckBox implements PropertyChangeListener {

    private final String text;

    public LCheckBox(String text) {
        this.text = text;
        init();
    }

    private void init() {
        ColorPalette.addThemeListener(this);
        setText(this.text);
        setBackground(ColorPalette.backgroundColor);
        setBorder(new CompoundBorder(
                new MatteBorder(5, 0, 5, 0, ColorPalette.inputUnderline),
                new EmptyBorder(0, 0, 0, 0)
        ));
        setForeground(ColorPalette.textColor);
        setFont(new Font("Dialog", Font.BOLD, 16));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LThemeChoice old = (LThemeChoice) evt.getOldValue();
        setBackground(ColorPalette.getNewColor(getBackground(), old));
        setBorder(new CompoundBorder(
                new MatteBorder(5, 0, 5, 0, ColorPalette.inputUnderline),
                new EmptyBorder(0, 0, 0, 0)
        ));
    }
}
