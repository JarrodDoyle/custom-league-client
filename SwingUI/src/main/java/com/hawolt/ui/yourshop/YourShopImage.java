package com.hawolt.ui.yourshop;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.resources.ledge.personalizedoffers.objects.PersonalizedOfferItem;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.store.IStoreImage;
import com.hawolt.util.paint.PaintHelper;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Objects;

/**
 * Created: 09/08/2023 20:09
 * Author: Twitter @hawolt
 **/

public class YourShopImage extends JComponent implements IStoreImage, ResourceConsumer<BufferedImage, byte[]> {
    private final PersonalizedOfferItem item;
    private BufferedImage image;

    public YourShopImage(PersonalizedOfferItem item) {
        this.item = item;
    }

    @Override
    public String getImageURL(InventoryType type, long itemId) {
        if (Objects.requireNonNull(type) == InventoryType.CHAMPION_SKIN) {
            return String.format(
                    "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/%s/%s.jpg",
                    item.getChampionId(),
                    itemId
            );
        }
        return String.format(
                "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/%s/%s.jpg",
                item.getChampionId(),
                item.getChampionId() * 1000
        );

    }

    public void load() {
        if (image != null) return;
        ResourceLoader.loadResource(getImageURL(item.getInventoryType(), item.getSkinId()), this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;
        Dimension dimension = getSize();
        int x = (dimension.width >> 1) - (image.getWidth() >> 1);
        int y = (dimension.height >> 1) - (image.getHeight() >> 1);
        g.drawImage(PaintHelper.circleize(image, ColorPalette.CARD_ROUNDING, x, y, dimension.width, dimension.height), 0, 0, null);
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load resource {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage image) {
        this.image = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, 200, 260);
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
