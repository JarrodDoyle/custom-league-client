package com.hawolt.ui.generic.component;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LTextPane extends JTextPane implements PropertyChangeListener {
    private SimpleAttributeSet attributeSet;

    public LTextPane(String text) {
        setText(text);
        init();
    }

    private void init() {
        ColorPalette.addThemeListener(this);
        this.attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(this.attributeSet, StyleConstants.ALIGN_CENTER);
        StyleConstants.setBackground(this.attributeSet, ColorPalette.accentColor);
        StyleConstants.setForeground(this.attributeSet, ColorPalette.textColor);
        StyleConstants.setFontFamily(this.attributeSet, "Dialog");
        StyleConstants.setFontSize(this.attributeSet, 16);
        StyleConstants.setBold(this.attributeSet, true);
        setParagraphAttributes(this.attributeSet, true);
        setBackground(ColorPalette.accentColor);
        setForeground(ColorPalette.textColor);
        setEditable(false);
    }

    public void setFontSize(int size) {
        StyleConstants.setFontSize(this.attributeSet, size);
        setParagraphAttributes(this.attributeSet, true);
    }

    public void setTextColor(Color color) {
        StyleConstants.setForeground(this.attributeSet, color);
        setParagraphAttributes(this.attributeSet, true);
    }

    public void setCrossedOutText(boolean value) {
        StyleConstants.setStrikeThrough(this.attributeSet, value);
        setParagraphAttributes(this.attributeSet, true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LThemeChoice old = (LThemeChoice) evt.getOldValue();
        setBackground(ColorPalette.getNewColor(getBackground(), old));
        setForeground(ColorPalette.getNewColor(getForeground(), old));
    }
}
