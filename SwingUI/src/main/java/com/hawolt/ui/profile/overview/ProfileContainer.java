package com.hawolt.ui.profile.overview;

import com.hawolt.async.Debouncer;
import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.resources.ledge.leagues.objects.RankedStatistic;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.client.resources.ledge.summoner.objects.SummonerProfile;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.profile.ProfileOverview;
import com.hawolt.util.paint.PaintHelper;
import com.hawolt.util.paint.jhlab.GaussianFilter;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created: 12/09/2023 19:54
 * Author: Twitter @hawolt
 **/

public class ProfileContainer extends ChildUIComponent implements ResourceConsumer<BufferedImage, byte[]> {

    private final CardLayout layout = new CardLayout();
    private final ChildUIComponent component = new ChildUIComponent(layout);
    private BufferedImage original, reference, mask;
    private SummonerProfile profile;

    private final ComponentAdapter adapter = new ComponentAdapter() {
        private final Debouncer debouncer = new Debouncer();

        @Override
        public void componentResized(ComponentEvent e) {
            debouncer.debounce("resize", () -> {
                ProfileContainer.this.reference = reconfigure();
                ProfileContainer.this.repaint();
            }, 100, TimeUnit.MILLISECONDS);
        }
    };

    public ProfileContainer(BufferedImage mask, Summoner summoner, SummonerProfile profile, RankedStatistic rankedStatistic) {
        super(new BorderLayout());
        this.addComponentListener(adapter);
        this.add(component, BorderLayout.CENTER);
        this.component.add("overview", new ProfileOverview(summoner, profile, rankedStatistic));
        this.component.setOpaque(false);
        this.profile = profile;
        this.mask = mask;
        this.initialize();
    }

    public void toggle(String name) {
        this.layout.show(component, name);
    }

    private void initialize() {
        int skinId = profile.getBackgroundSkinId();
        if (skinId != 0) {
            int championId = (int) Math.floor(skinId / 1000D);
            String uri = String.format(SPRITE_PATH, championId, skinId);
            ResourceLoader.loadResource(uri, this);
        }
    }

    private BufferedImage reconfigure() {
        Dimension dimension = getSize();
        BufferedImage initial = Scalr.resize(original, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, dimension.height);
        BufferedImage fitted = initial.getWidth() >= dimension.width ? initial : Scalr.resize(initial, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, dimension.width);
        GaussianFilter gaussianFilter = new GaussianFilter(7);
        return PaintHelper.mask(gaussianFilter.filter(fitted, null), mask);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (reference == null) return;
        Dimension dimension = getSize();
        int imageX = (dimension.width >> 1) - (reference.getWidth() >> 1);
        int imageY = (dimension.height >> 1) - (reference.getHeight() >> 1);
        g.drawImage(reference, imageX, imageY, null);
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage bufferedImage) {
        this.original = bufferedImage;
        this.reference = reconfigure();
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    private static final String SPRITE_PATH = "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/%s/%s.jpg";
}
