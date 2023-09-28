package com.hawolt.ui.generic.component;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LTextPane extends JTextPane implements PropertyChangeListener {

    public LTextPane(String text) {
        setText(text);
        init();
    }

    private void init() {
        ColorPalette.addThemeListener(this);
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
        StyleConstants.setBackground(attributeSet, ColorPalette.accentColor);
        StyleConstants.setForeground(attributeSet, ColorPalette.textColor);
        StyleConstants.setFontFamily(attributeSet, "Dialog");
        StyleConstants.setFontSize(attributeSet, 16);
        StyleConstants.setBold(attributeSet, true);
        setParagraphAttributes(attributeSet, true);
        setBackground(ColorPalette.accentColor);
        setForeground(ColorPalette.textColor);
        setEditable(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LThemeChoice old = (LThemeChoice) evt.getOldValue();
        setBackground(ColorPalette.getNewColor(getBackground(), old));
        setForeground(ColorPalette.getNewColor(getForeground(), old));
    }
}
