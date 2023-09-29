package com.hawolt.ui.generic.translucent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created: 24/09/2023 05:41
 * Author: Twitter @hawolt
 **/

public class TranslucentComponent extends JComponent implements PropertyChangeListener {
    protected static final Font DEFAULT_FONT = new Font(Font.DIALOG, Font.BOLD, 24);
    public final MouseAdapter adapter = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            TranslucentComponent.this.firePropertyChange("mouseHover", false, true);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            TranslucentComponent.this.firePropertyChange("mouseHover", true, false);
        }
    };
    protected boolean mouseHover;

    public TranslucentComponent() {
        this.addPropertyChangeListener(this);
        this.addMouseMotionListener(adapter);
        this.addMouseListener(adapter);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    public boolean isMouseHover() {
        return mouseHover;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "mouseHover" -> TranslucentComponent.this.mouseHover = (boolean) evt.getNewValue();
        }
        this.repaint();
    }
}
