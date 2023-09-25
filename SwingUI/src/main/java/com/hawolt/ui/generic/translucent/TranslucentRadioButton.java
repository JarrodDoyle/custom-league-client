package com.hawolt.ui.generic.translucent;

import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.utility.HighlightType;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

/**
 * Created: 24/09/2023 05:41
 * Author: Twitter @hawolt
 **/

public class TranslucentRadioButton extends TranslucentButton {
    private final static int selectedIndicatorSize = 5;
    private final HighlightType highlightType;
    private boolean active;

    public TranslucentRadioButton(String text) {
        this(text, LTextAlign.CENTER, HighlightType.COMPONENT);
    }

    public TranslucentRadioButton(String text, LTextAlign textAlign) {
        this(text, textAlign, HighlightType.COMPONENT);
    }

    public TranslucentRadioButton(String text, HighlightType highlightType) {
        this(text, LTextAlign.CENTER, highlightType);
    }

    public TranslucentRadioButton(String text, LTextAlign textAlign, HighlightType highlightType) {
        super(text, textAlign);
        this.addMouseListener(adapter);
        this.highlightType = highlightType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (active) {
            Graphics2D graphics2D = (Graphics2D) g;
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = 0, y = 0, width = getWidth(), height = getHeight();
            switch (highlightType) {
                case LEFT:
                    width = selectedIndicatorSize;
                    break;
                case TOP:
                    height = selectedIndicatorSize;
                    break;
                case RIGHT:
                    x = width - selectedIndicatorSize;
                    width = selectedIndicatorSize;
                    break;
                case BOTTOM:
                    y = height - selectedIndicatorSize;
                    height = selectedIndicatorSize;
                    x = selectedIndicatorSize;
                    width = width - (selectedIndicatorSize << 1);
                    break;
                case TEXT:
                    width = 0;
                    height = 0;
            }
            Area buttonArea = new Area(new RoundRectangle2D.Double(x, y, width, height, 0, 0));
            graphics2D.setColor(Color.WHITE);
            graphics2D.fill(buttonArea);
        }
        Color color = mousePressed ? Color.LIGHT_GRAY : active ? Color.GRAY : mouseHover ? Color.DARK_GRAY : Color.WHITE;
        this.setForeground(color);
        super.paintComponent(g);
    }
}
