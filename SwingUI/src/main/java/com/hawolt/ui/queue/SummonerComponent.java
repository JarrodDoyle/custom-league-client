package com.hawolt.ui.queue;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.resources.ledge.parties.objects.PartyParticipant;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.paint.PaintHelper;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created: 21/08/2023 18:34
 * Author: Twitter @hawolt
 **/

public class SummonerComponent extends ChildUIComponent implements ResourceConsumer<BufferedImage, byte[]> {

    public static final String CD_PP_BASE = "https://raw.communitydragon.org/latest/game/assets/ux/summonericons/profileicon%s.png";
    public static final Dimension IMAGE_DIMENSION = new Dimension(128, 128);
    public static final Font NAME_FONT = new Font("Arial", Font.BOLD, 24);
    public PartyParticipant participant;
    public BufferedImage image;
    public Summoner summoner;

    private Color accent = ColorPalette.accentColor;

    public SummonerComponent() {
        super(null);
        ColorPalette.addThemeListener(evt -> {
            LThemeChoice old = (LThemeChoice) evt.getOldValue();
            accent = ColorPalette.getNewColor(accent, old);
        });
    }

    public void update(PartyParticipant participant, Summoner summoner) {
        this.participant = participant;
        this.summoner = summoner;
        this.repaint();
        if (summoner == null) return;
        ResourceLoader.loadResource(String.format(CD_PP_BASE, summoner.getProfileIconId()), this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dimensions = getSize();
        Graphics2D g2d = (Graphics2D) g;

        // We don't want to draw anything if there's nobody actually here
        if (summoner == null || participant == null) return;
        String role = participant.getRole();
        if (!role.equals("MEMBER") && !role.equals("LEADER")) return;

        // Enable antialisaing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw main card
        g.setColor(ColorPalette.cardColor);
        PaintHelper.roundedSquare(g2d, 0, 0, dimensions.width, dimensions.height, ColorPalette.CARD_ROUNDING, true, true, true, true);

        // Draw summoner icon
        int centeredX = dimensions.width >> 1;
        int centeredY = dimensions.height >> 1;
        if (image != null) {
            int imageW = image.getWidth();
            int imageH = image.getHeight();
            int imageX = centeredX - (imageW >> 1);
            int imageY = (dimensions.height >> 1) - (imageH >> 1);

            // Placeholder background in case something funky is happening with the image
            g.setColor(ColorPalette.backgroundColor);
            PaintHelper.roundedSquare(g2d, imageX, imageY, imageW, imageH, ColorPalette.CARD_ROUNDING, true, true, true, true);

            // Main icon
            g.drawImage(PaintHelper.circleize(image, ColorPalette.CARD_ROUNDING), imageX, imageY, null);
        }

        // Summoner name
        g2d.setFont(NAME_FONT);
        g2d.setColor(Color.WHITE);
        FontMetrics metrics = g2d.getFontMetrics();
        String name = summoner.getName().trim();
        int width = metrics.stringWidth(name);
        int nameX = centeredX - (width >> 1);
        g.drawString(name, nameX, centeredY - (IMAGE_DIMENSION.height >> 1) - 20);

        // Draw leader indicator
        if (role.equals("LEADER")){
            g2d.setColor(Color.ORANGE);
            g.drawString("P", 8, 8 + metrics.getAscent());
        }
    }


    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load resource {}, {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage image) {
        if (image.getHeight() != 128 || image.getWidth() != 128) {
            this.image = Scalr.resize(
                    image,
                    Scalr.Method.ULTRA_QUALITY,
                    Scalr.Mode.FIT_TO_HEIGHT,
                    IMAGE_DIMENSION.width,
                    IMAGE_DIMENSION.height
            );
        } else {
            this.image = image;
        }
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
