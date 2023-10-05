package com.hawolt.ui.yourshop;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.cache.CachedValueLoader;
import com.hawolt.client.resources.ledge.personalizedoffers.PersonalizedOffersLedge;
import com.hawolt.client.resources.ledge.personalizedoffers.objects.PersonalizedOfferItem;
import com.hawolt.client.resources.ledge.store.StoreLedge;
import com.hawolt.client.resources.ledge.store.objects.Wallet;
import com.hawolt.client.resources.purchasewidget.CurrencyType;
import com.hawolt.ui.generic.component.LTextPane;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.store.IStoreElement;
import com.hawolt.ui.store.StoreButton;
import com.hawolt.util.audio.AudioEngine;
import com.hawolt.util.audio.Sound;
import org.jetbrains.annotations.NotNull;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class YourShopElement extends ChildUIComponent implements IStoreElement {

    private final ChildUIComponent buttonComponent = new ChildUIComponent(new GridBagLayout());
    private final ChildUIComponent mainComponent = new ChildUIComponent(new GridBagLayout());
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final PersonalizedOfferItem item;
    private ChildUIComponent infoComponent;
    private final YourShopImage image;
    private final LeagueClient client;

    public YourShopElement(LeagueClient client, PersonalizedOfferItem item) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(15, 15, 15, 15));
        this.add(image = new YourShopImage(item), BorderLayout.CENTER);
        this.setPreferredSize(new Dimension(150, 400));
        this.setBackground(ColorPalette.cardColor);
        this.client = client;
        this.item = item;
        this.build();
    }

    private void build() {
        image.load();
        mainComponent.setBackground(ColorPalette.cardColor);
        infoComponent = getInfoComponent();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainComponent.add(infoComponent, gbc);
        buttonComponent.setBackground(ColorPalette.cardColor);
        gbc.insets = new Insets(0, 2, 0, 2);
        if (item.getDiscountPrice() > 0) {
            StoreButton button = new StoreButton(this, CurrencyType.RP, item.getDiscountPrice());
            button.setHighlightColor(ColorPalette.buttonSelectionAltColor);
            button.setBackground(ColorPalette.buttonSelectionColor);
            button.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
            buttonComponent.add(button, gbc);
            gbc.gridwidth = 2;
            gbc.gridy = 1;
            mainComponent.add(buttonComponent, gbc);
        } else {
            gbc.gridwidth = 2;
            gbc.gridy = 1;
            LTextPane owned = new LTextPane("OWNED");
            owned.setBackground(ColorPalette.cardColor);
            mainComponent.add(owned, gbc);
        }
        add(mainComponent, BorderLayout.SOUTH);
        revalidate();
    }

    @NotNull
    private ChildUIComponent getInfoComponent() {
        ChildUIComponent infoComponent = new ChildUIComponent(new GridLayout(3, 1, 0, 2));
        infoComponent.setBackground(ColorPalette.cardColor);
        LTextPane discount = new LTextPane("-" + this.item.getDiscountAmount() + "%");
        discount.setTextColor(ColorPalette.discountColor);
        discount.setFontSize(18);
        discount.setBackground(ColorPalette.cardColor);
        infoComponent.add(discount);
        LTextPane name = new LTextPane(this.item.getName());
        name.setBackground(ColorPalette.cardColor);
        infoComponent.add(name);
        if(item.getDiscountPrice() > 0) {
            LTextPane original = new LTextPane(this.item.getOriginalPrice() + " RP");
            original.setTextColor(ColorPalette.oldPriceColor);
            original.setBackground(ColorPalette.cardColor);
            original.setCrossedOutText(true);
            infoComponent.add(original);
        }
        return infoComponent;
    }

    public PersonalizedOfferItem getItem() {
        return item;
    }

    public int getItemSpotIndex() {
        return item.getSpotIndex();
    }

    public YourShopImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(),
                ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0, ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0);
        g2d.setColor(ColorPalette.discountColor);
        g2d.setStroke(new BasicStroke(6));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0, ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0);
        g2d.setStroke(new BasicStroke(2));
    }

    @Override
    public void purchase(CurrencyType currencyType, long price) {
        Swiftrift.service.execute(() -> {
            try {
                PersonalizedOffersLedge ledge = client.getLedge().getPersonalizedOffers();
                StoreLedge balance = client.getLedge().getStore();
                List<String> offers = item.getOfferIds().get(item.getSpotIndex());
                List<Boolean> owned = item.getOwned().get(item.getSpotIndex());
                String message = String.format(
                        "Do you want to spend %s %s for this purchase?",
                        price,
                        currencyType == CurrencyType.RP ? "Riot Points" : "Blue Essence"
                );
                int result = Swiftrift.showOptionDialog(message, "YES", "NO");
                if (result != 0) return;
                Wallet wallet = balance.getBalanceV2();
                if (!(wallet.getRiotPoints() >= item.getDiscountPrice())) {
                    AudioEngine.play(Sound.ERROR);
                    return;
                }
                for (int i = 0; i < offers.size(); i++) {
                    if (owned.get(i).equals(true)) continue;
                    ledge.accept(offers.get(i));
                    AudioEngine.play(Sound.SUCCESS);
                    buttonComponent.removeAll();
                    infoComponent.remove(2);
                    LTextPane own = new LTextPane("OWNED");
                    own.setBackground(ColorPalette.cardColor);
                    mainComponent.add(own, gbc);
                    Swiftrift.service.execute(
                            new CachedValueLoader<>(
                                    CacheElement.INVENTORY_TOKEN,
                                    () -> client.getLedge().getInventoryService().getInventoryToken(),
                                    client
                            )
                    );
                    revalidate();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
