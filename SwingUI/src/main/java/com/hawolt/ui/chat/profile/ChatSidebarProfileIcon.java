package com.hawolt.ui.chat.profile;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.paint.PaintHelper;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created: 08/08/2023 17:28
 * Author: Twitter @hawolt
 **/

public class ChatSidebarProfileIcon extends ChildUIComponent implements ResourceConsumer<BufferedImage, byte[]> {
    private final String ICON_BASE_URL = "https://raw.communitydragon.org/latest/game/assets/ux/summonericons/profileicon%s.png";
    private final int ICON_SIZE = 64;
    private BufferedImage icon;

    public ChatSidebarProfileIcon(UserInformation information, LayoutManager layout) {
        super(layout);
        this.setBackground(ColorPalette.accentColor);
        this.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        this.setBorder(new EmptyBorder(8, 8, 8, 8));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int y = getHeight() - ICON_SIZE;
        g.setColor(ColorPalette.backgroundColor);
        PaintHelper.roundedSquare((Graphics2D) g, 0, y, ICON_SIZE, ICON_SIZE, ColorPalette.CARD_ROUNDING, true, true, true, true);
        if (icon != null) {
            g.drawImage(PaintHelper.circleize(icon, ColorPalette.CARD_ROUNDING), 0, y, null);
        }
    }

    public void setIconId(long iconId) {
        ResourceLoader.loadResource(String.format(ICON_BASE_URL, iconId), this);
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load resource {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage image) {
        this.icon = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, ICON_SIZE, ICON_SIZE);
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
