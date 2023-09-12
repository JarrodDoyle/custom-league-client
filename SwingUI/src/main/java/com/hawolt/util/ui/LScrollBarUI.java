package com.hawolt.util.ui;

import com.hawolt.util.ColorPalette;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class LScrollBarUI extends BasicScrollBarUI implements PropertyChangeListener {
    private final int scrollSize = 14;
    private final MouseAdapter mouseEvent;
    private float animate;
    private boolean show;
    private boolean hover;
    private boolean press;

    public LScrollBarUI() {
        ColorPalette.addThemeListener(this);
        mouseEvent = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                press = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                press = false;
                if (!hover) {
                    start(false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                if (!show) {
                    start(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                if (!press) {
                    start(false);
                }
            }
        };
    }

    public static ComponentUI createUI(JComponent c) {
        return new LScrollBarUI();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        scrollbar.setForeground(ColorPalette.scrollHandleColor);
    }

    @Override
    public void installUI(JComponent component) {
        super.installUI(component);
        component.setPreferredSize(new Dimension(scrollSize, scrollSize));
        component.addMouseListener(mouseEvent);
        component.setForeground(ColorPalette.scrollHandleColor);
    }

    private void start(boolean show) {
        this.show = show;
    }

    //Made to disable arrow buttons
    protected JButton CreateZeroButton() {
        JButton button = new JButton("zero button");
        Dimension zeroDim = new Dimension(0, 0);
        button.setPreferredSize(zeroDim);
        button.setMinimumSize(zeroDim);
        button.setMaximumSize(zeroDim);
        return button;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return CreateZeroButton();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return CreateZeroButton();
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.SrcOver.derive(animate * 0.5f));
        g2.setColor(scrollbar.getForeground().brighter());
        g2.fill(new Rectangle2D.Double(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height));
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(scrollbar.getForeground());
        double border = scrollSize * 0.3f - (animate * 1);
        double sp = 10 * animate;
        g2.setComposite(AlphaComposite.SrcOver.derive(1f - (1f - animate) * 0.3f));
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            double width = thumbBounds.getWidth() - border * 2;
            double height = thumbBounds.getHeight() - sp * 2;
            g2.fill(new RoundRectangle2D.Double(thumbBounds.x + border, thumbBounds.y + sp, width, height, width, width));
        } else {
            double width = thumbBounds.getWidth() - sp * 2;
            double height = thumbBounds.getHeight() - border * 2;
            g2.fill(new RoundRectangle2D.Double(thumbBounds.x + sp, thumbBounds.y + border, width, height, height, height));
        }
        g2.dispose();
        g.dispose();
    }

    private class ScrollButton extends JButton {

        private final int orientation;
        private final boolean isIncrease;
        private boolean mouseHover;
        private boolean mousePress;

        public ScrollButton(int orientation, boolean isIncrease) {
            this.orientation = orientation;
            this.isIncrease = isIncrease;
            setContentAreaFilled(false);
            setPreferredSize(new Dimension(scrollSize, scrollSize));
            List<Point2D> points = new ArrayList<>();
            double width = scrollSize * 0.8f;
            double height = scrollSize * 0.7f;
            if (orientation == JScrollBar.VERTICAL) {
                if (isIncrease) {
                    points.add(new Point2D.Double(width / 2, height));
                    points.add(new Point2D.Double(width, 0));
                    points.add(new Point2D.Double(0, 0));
                } else {
                    points.add(new Point2D.Double(width / 2, 0));
                    points.add(new Point2D.Double(width, height));
                    points.add(new Point2D.Double(0, height));
                }
            } else {
                if (isIncrease) {
                    points.add(new Point2D.Double(0, 0));
                    points.add(new Point2D.Double(width, height / 2));
                    points.add(new Point2D.Double(0, height));
                } else {
                    points.add(new Point2D.Double(width, 0));
                    points.add(new Point2D.Double(0, height / 2));
                    points.add(new Point2D.Double(width, height));
                }
            }
            addMouseListener(mouseEvent);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    mouseHover = true;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    mouseHover = false;
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    mousePress = true;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    mousePress = false;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.SrcOver.derive(animate * 0.5f));
            g2.setColor(scrollbar.getForeground().brighter());
            int x = 0;
            int y = isIncrease ? 8 : 0;
            int width = getWidth();
            int height = getHeight() - 8;
            if (orientation == JScrollBar.VERTICAL) {
                g2.fill(new Rectangle2D.Double(x, y, width, height));
            } else {
                g2.fill(new Rectangle2D.Double(y, x, height, width));
            }
            g2.setComposite(AlphaComposite.SrcOver.derive(animate));
            if (mousePress) {
                g2.setColor(new Color(110, 110, 110));
            } else if (mouseHover) {
                g2.setColor(new Color(130, 130, 130));
            } else {
                g2.setColor(scrollbar.getForeground());
            }
            double ax = scrollSize * 0.1f;
            double ay = scrollSize * 0.15f;
            g2.translate(ax, ay);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
