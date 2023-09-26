package com.hawolt.ui.generic.component;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created: 27/09/2023 00:28
 * Author: Twitter @hawolt
 **/

public class SwiftLabel extends JLabel implements PropertyChangeListener {
    public SwiftLabel() {
        this("");
    }

    public SwiftLabel(String text) {
        this(text, SwingConstants.CENTER);
    }

    public SwiftLabel(String text, int constant) {
        super(text, null, constant);
        ColorPalette.addThemeListener(this);
        setFont(new Font("Dialog", Font.BOLD, 14));
        setBackground(ColorPalette.backgroundColor);
        setForeground(ColorPalette.textColor);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LThemeChoice old = (LThemeChoice) evt.getOldValue();
        setBackground(ColorPalette.getNewColor(getBackground(), old));
        setForeground(ColorPalette.getNewColor(getForeground(), old));
    }
}
