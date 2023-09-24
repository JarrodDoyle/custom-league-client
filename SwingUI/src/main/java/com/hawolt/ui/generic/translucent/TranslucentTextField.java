package com.hawolt.ui.generic.translucent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 24/09/2023 07:03
 * Author: Twitter @hawolt
 **/

public class TranslucentTextField extends JTextField {
    private final Font DEFAULT_FONT = new Font(Font.DIALOG, Font.BOLD, 24);
    private final String hint;

    public TranslucentTextField() {
        this(null);
    }

    public TranslucentTextField(String hint) {
        super(13);
        this.setBorder(new EmptyBorder(0, 5, 0, 5));
        this.setForeground(Color.WHITE);
        this.setFont(DEFAULT_FONT);
        this.setOpaque(false);
        this.hint = hint;
    }

    @Override
    public void setBorder(Border border) {
        Border compoundBorder = new CompoundBorder(
                border,
                new EmptyBorder(0, 5, 0, 5)
        );
        super.setBorder(compoundBorder);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
        if (!getText().isEmpty() || hint == null) return;
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Insets insets = getInsets();
        FontMetrics fontMetrics = g.getFontMetrics();
        int background = getBackground().getRGB();
        int foreground = getForeground().getRGB();
        int mask = 0xFEFEFEFE;
        int blend = ((background & mask) >>> 1) + ((foreground & mask) >>> 1);
        g.setColor(new Color(blend, true));
        g.drawString(hint, insets.left, getHeight() / 2 + fontMetrics.getAscent() / 2 - 2);
    }
}
