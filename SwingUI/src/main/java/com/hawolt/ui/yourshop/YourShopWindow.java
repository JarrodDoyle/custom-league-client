package com.hawolt.ui.yourshop;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.personalizedoffers.objects.PersonalOfferCatalog;
import com.hawolt.client.resources.ledge.personalizedoffers.objects.PersonalizedOfferItem;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LTextPane;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

/**
 * Created: 05/10/2023 11:56
 * Author: Twitter @hawolt
 **/

public class YourShopWindow extends ChildUIComponent {
    private final Map<Long, YourShopElement> yourShopElementMap = new HashMap<>();
    private final LeagueClient client;
    private ChildUIComponent grid;

    public YourShopWindow(LeagueClient client) {
        super(new BorderLayout(0, 5));
        this.client = client;
    }

    public void build(JSONObject offers) {
        setBorder(new EmptyBorder(125, 10, 125, 10));
        add(createMainComponent(offers), BorderLayout.CENTER);
        PersonalOfferCatalog personalCatalog = new PersonalOfferCatalog();
        addOffers(personalCatalog.getPersonalOfferCatalog(client));
    }

    private ChildUIComponent createMainComponent(JSONObject offers) {
        ChildUIComponent component = new ChildUIComponent(new BorderLayout(0, 30));
        grid = new ChildUIComponent(new GridLayout(1, 6, 15, 15));
        component.add(grid, BorderLayout.NORTH);
        ChildUIComponent inputPanel = createYourShopInfo(offers);
        component.add(inputPanel, BorderLayout.SOUTH);
        return component;
    }


    private ChildUIComponent createYourShopInfo(JSONObject object) {
        ChildUIComponent yourShopInfo = new ChildUIComponent(new GridLayout(2, 0, 0, 0));
        yourShopInfo.setBackground(ColorPalette.backgroundColor);
        yourShopInfo.setBorder(new EmptyBorder(5, 5, 5, 5));

        String date = object.getJSONObject("promotion").getString("endTime");
        try {
            Date endDate = Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(date)));
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM", Locale.US);
            SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm z", Locale.US);
            String offers = "Offers expire " + formatter.format(endDate) + " at " + formatter2.format(endDate);
            LTextPane dateFormatted = new LTextPane(offers);
            dateFormatted.setBackground(ColorPalette.backgroundColor);
            yourShopInfo.add(dateFormatted, BorderLayout.NORTH);
            LTextPane info = new LTextPane("Offers include Champion if unowned.");
            info.setFontSize(14);
            info.setBackground(ColorPalette.backgroundColor);
            yourShopInfo.add(info, BorderLayout.SOUTH);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return yourShopInfo;
    }


    public void addOffers(List<PersonalizedOfferItem> items) {
        try {
            for (PersonalizedOfferItem item : items) {
                long itemId = item.getSkinId();
                YourShopElement element = new YourShopElement(client, item);
                yourShopElementMap.put(itemId, element);
                grid.add(element);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        updateYourShopElements();
    }

    public void updateYourShopElements() {
        grid.removeAll();
        yourShopElementMap.values()
                .stream()
                .sorted(Comparator.comparingInt(YourShopElement::getItemSpotIndex))
                .forEach(this.grid::add);
        revalidate();
        repaint();
    }
}
