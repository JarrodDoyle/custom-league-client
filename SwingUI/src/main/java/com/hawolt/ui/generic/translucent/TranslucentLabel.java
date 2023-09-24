package com.hawolt.ui.generic.translucent;

import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.util.paint.PaintHelper;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;

/**
 * Created: 24/09/2023 05:41
 * Author: Twitter @hawolt
 **/

public class TranslucentLabel extends TranslucentComponent {
    private final LTextAlign textAlign;
    protected final String text;

    public TranslucentLabel(String text) {
        this(text, LTextAlign.CENTER);
    }

    public TranslucentLabel(String text, LTextAlign textAlign) {
        this.setForeground(Color.WHITE);
        this.textAlign = textAlign;
        this.text = text;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        PaintHelper.drawShadowText(graphics2D, DEFAULT_FONT, text, getBounds(), textAlign, getForeground());
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension d = getPreferredSize();
        View v = (View) this.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d.width -= v.getPreferredSpan(View.X_AXIS) - v.getMinimumSpan(View.X_AXIS);
        }
        return d;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = getPreferredSize();
        View v = (View) this.getClientProperty(BasicHTML.propertyKey);
        if (v != null) {
            d.width += v.getMaximumSpan(View.X_AXIS) - v.getPreferredSpan(View.X_AXIS);
        }
        return d;
    }

    @Override
    public Dimension getPreferredSize() {
        String text = this.text;

        FontMetrics fm = this.getFontMetrics(DEFAULT_FONT);

        Rectangle iconR = new Rectangle();
        Rectangle textR = new Rectangle();
        Rectangle viewR = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);

        SwingUtilities.layoutCompoundLabel(
                this, fm, text, null,
                SwingConstants.CENTER, SwingConstants.CENTER,
                SwingConstants.CENTER, SwingConstants.TRAILING,
                viewR, iconR, textR, 0
        );

        textR = new Rectangle(
                textR.x,
                textR.y,
                textR.width + ((textR.width / 20) << 1),
                textR.height + (fm.getAscent() >> 1)
        );

        Rectangle r = iconR.union(textR);

        Insets insets = this.getInsets();
        r.width += insets.left + insets.right;
        r.height += insets.top + insets.bottom;

        return r.getSize();
    }
}
