package com.hawolt.ui.generic.translucent;

import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.util.paint.PaintHelper;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created: 24/09/2023 05:41
 * Author: Twitter @hawolt
 **/

public class TranslucentButton extends TranslucentComponent implements PropertyChangeListener {
    public final MouseAdapter adapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            TranslucentButton.this.firePropertyChange("buttonPressed", false, true);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            TranslucentButton.this.firePropertyChange("buttonPressed", true, false);
        }
    };
    protected final String text;
    private final LTextAlign textAlign;
    protected boolean mousePressed;

    public TranslucentButton(String text) {
        this(text, LTextAlign.CENTER);
    }

    public TranslucentButton(String text, LTextAlign textAlign) {
        this.setForeground(Color.WHITE);
        this.addMouseListener(adapter);
        this.textAlign = textAlign;
        this.text = text;
    }

    public void addActionListener(ActionListener listener) {
        this.listenerList.add(ActionListener.class, listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "buttonPressed" -> {
                TranslucentButton.this.mousePressed = (boolean) evt.getNewValue();
                if (TranslucentButton.this.mousePressed) {
                    this.fireActionPerformed(new ActionEvent(TranslucentButton.this, ActionEvent.ACTION_PERFORMED, evt.getPropertyName()));
                }
            }
        }
        super.propertyChange(evt);
    }

    protected void fireActionPerformed(ActionEvent event) {
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                if (e == null) {
                    e = new ActionEvent(TranslucentButton.this,
                            ActionEvent.ACTION_PERFORMED,
                            getClass().getSimpleName(),
                            event.getWhen(),
                            event.getModifiers());
                }
                ((ActionListener) listeners[i + 1]).actionPerformed(e);
            }
        }
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
