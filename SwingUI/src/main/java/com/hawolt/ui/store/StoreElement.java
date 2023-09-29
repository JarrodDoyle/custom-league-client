package com.hawolt.ui.store;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.cache.CachedValueLoader;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.purchasewidget.CurrencyType;
import com.hawolt.client.resources.purchasewidget.PurchaseWidget;
import com.hawolt.ui.generic.component.LTextPane;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.audio.AudioEngine;
import com.hawolt.util.audio.Sound;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 09/08/2023 19:01
 * Author: Twitter @hawolt
 **/

public class StoreElement extends ChildUIComponent implements IStoreElement {
    private final List<StoreButton> buttons = new ArrayList<>();
    private final LeagueClient client;
    private final StoreImage image;
    private final IStorePage page;
    private final StoreItem item;
    private final boolean owned;

    public StoreElement(LeagueClient client, IStorePage page, StoreItem item, boolean owned) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(15, 15, 15, 15));
        this.add(image = new StoreImage(item), BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(150, 300));
        this.setBackground(ColorPalette.cardColor);
        this.client = client;
        this.owned = owned;
        this.item = item;
        this.page = page;
        this.build();
    }

    private void build() {
        if (item.isBlueEssencePurchaseAvailable() && item.getCorrectBlueEssenceCost() > 0) {
            StoreButton button = new StoreButton(this, CurrencyType.IP, item.getCorrectBlueEssenceCost());
            button.setHighlightColor(ColorPalette.buttonSelectionAltColor);
            button.setBackground(ColorPalette.buttonSelectionColor);
            button.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
            buttons.add(button);
        }
        if (item.isRiotPointPurchaseAvailable() && item.getCorrectRiotPointCost() > 0) {
            StoreButton button = new StoreButton(this, CurrencyType.RP, item.getCorrectRiotPointCost());
            button.setHighlightColor(ColorPalette.buttonSelectionAltColor);
            button.setBackground(ColorPalette.buttonSelectionColor);
            button.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
            buttons.add(button);
        }
        ChildUIComponent mainComponent = new ChildUIComponent(new GridBagLayout());
        mainComponent.setBackground(ColorPalette.cardColor);
        GridBagConstraints gbc = new GridBagConstraints();
        ChildUIComponent nameComponent = new ChildUIComponent(new GridLayout(1, 0, 0, 0));
        LTextPane name = new LTextPane(this.item.getName());
        nameComponent.add(name);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainComponent.add(nameComponent, gbc);
        ChildUIComponent buttonComponent = new ChildUIComponent(new GridBagLayout());
        buttonComponent.setBackground(ColorPalette.cardColor);
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.gridwidth = 1;
        if (!owned) {
            for (StoreButton button : buttons) {
                gbc.gridx = buttons.indexOf(button);
                buttonComponent.add(button, gbc);
            }
        }
        gbc.gridwidth = 2;
        gbc.gridy = 1;
        mainComponent.add(buttonComponent, gbc);
        add(mainComponent, BorderLayout.SOUTH);
        revalidate();
    }

    public StoreItem getItem() {
        return item;
    }

    public StoreImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(),
                ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0, ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0);

    }

    @Override
    public void purchase(CurrencyType currencyType, long price) {
        Swiftrift.service.execute(() -> {
            try {
                PurchaseWidget widget = client.getPurchaseWidget();
                String message = String.format(
                        "Do you want to spend %s %s for this purchase?",
                        price,
                        currencyType == CurrencyType.RP ? "Riot Points" : "Blue Essence"
                );
                int result = Swiftrift.showOptionDialog(message, "YES", "NO");
                if (result != 0) return;
                JSONObject response;
                if (item.ownVariantId()) {
                    response = new JSONObject(widget.purchase(currencyType, InventoryType.BUNDLES, item.getVariantBundleId(), price));
                } else {
                    response = new JSONObject(widget.purchase(currencyType, item.getInventoryType(), item.getItemId(), price));
                }
                if (response.has("errorCode")) {
                    AudioEngine.play(Sound.ERROR);
                } else {
                    AudioEngine.play(Sound.SUCCESS);
                    page.removeStoreElement(this);
                    Swiftrift.service.execute(
                            new CachedValueLoader<>(
                                    CacheElement.INVENTORY_TOKEN,
                                    () -> client.getLedge().getInventoryService().getInventoryToken(),
                                    client
                            )
                    );
                    revalidate();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}