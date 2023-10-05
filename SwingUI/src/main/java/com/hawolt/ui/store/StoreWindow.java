package com.hawolt.ui.store;

import com.hawolt.Swiftrift;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.personalizedoffers.objects.PersonalOfferCatalog;
import com.hawolt.client.resources.ledge.personalizedoffers.objects.PersonalizedOfferItem;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.client.resources.ledge.store.objects.StoreCatalog;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.ledge.store.objects.StoreSortProperty;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LTabbedPane;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Created: 09/08/2023 17:44
 * Author: Twitter @hawolt
 **/

public class StoreWindow extends ChildUIComponent implements Runnable {
    private final String[] shopTabsType = {InventoryType.CHAMPION.name(), InventoryType.CHAMPION_SKIN.name(), InventoryType.COMPANION.name(), InventoryType.EVENT_PASS.name()};
    private final String[] shopTabsName = {"CHAMPIONS", "SKINS", "TFT", "PASSES & BOOSTS"};
    private final Map<InventoryType, List<StoreItem>> cache = new HashMap<>();
    private final LeagueClient client;

    private final LTabbedPane pane;

    public StoreWindow(LeagueClient client) {
        super(new BorderLayout());
        this.client = client;
        this.pane = new LTabbedPane();
        try {
            String jwt = client.getLedge().getInventoryService().getInventoryToken();
            JSONObject object = new JSONObject(new String(Base64.getDecoder().decode(jwt.split("\\.")[1])));
            JSONObject items = object.getJSONObject("items");
            for (int i = 0; i < shopTabsName.length; i++) {
                pane.addTab(
                        shopTabsType[i],
                        shopTabsName[i],
                        createStorePage(
                                client,
                                shopTabsType[i],
                                shopTabsName[i],
                                items
                        )
                );
            }
            //get better at handling lots of images - I'm too stupid ~Lett4s
            //no you are not, I fixed this for you :) ~hawolt
            //kneel and bow down to King hawolt ~Lett4s
        } catch (Exception e) {
            Logger.error(e);
        }
        add(pane, BorderLayout.CENTER);

        Swiftrift.service.execute(this);
    }

    public StorePage getTabByType(String type) {
        for (int i = 0; i < pane.getTabCount(); i++) {
            if (pane.getType(i).equals(type)) {
                return (StorePage) pane.getComponentAt(i);
            }
        }
        return null;
    }

    public StorePage createStorePage(LeagueClient client, String type, String name, JSONObject items) {
        JSONArray itemTypeArray = items.getJSONArray(type);
        if (type.equals(InventoryType.CHAMPION_SKIN.name())) {
            if (items.has("VINTAGE_CHAMPION_SKIN")) {
                JSONArray vintageSkinArray = items.getJSONArray("VINTAGE_CHAMPION_SKIN");
                for (int i = 0; i < vintageSkinArray.length(); i++) {
                    itemTypeArray.put(vintageSkinArray.get(i));
                }
            }
        }
        List<Long> itemTypeList = itemTypeArray.toList()
                .stream()
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();
        return new StorePage(
                client,
                name,
                itemTypeList,
                StoreSortProperty.values()
        );
    }

    @Override
    public void run() {
        StoreCatalog catalog = client.getCachedValue(CacheElement.STORE_CATALOG);
        List<StoreItem> list = catalog.getList();
        Map<InventoryType, StorePage> map = new HashMap<>();
        Map<InventoryType, List<StoreItem>> matches = new HashMap<>();
        for (StoreItem item : list) {
            InventoryType type = item.getInventoryType();
            if (item.isChibiCompanion(item.asJSON()) || type.equals(InventoryType.TFT_MAP_SKIN))
                type = InventoryType.COMPANION;
            if (type.equals(InventoryType.BOOST)) {
                type = InventoryType.EVENT_PASS;
            }
            if (!map.containsKey(type)) map.put(type, getTabByType(type.name()));
            StorePage page = map.get(type);
            if (page == null) continue;
            if (!cache.containsKey(type)) cache.put(type, new ArrayList<>());
            cache.get(type).add(item);
            if (!matches.containsKey(type)) matches.put(type, new ArrayList<>());
            matches.get(type).add(item);
        }
        for (InventoryType type : matches.keySet()) {
            StorePage page = map.get(type);
            if (page == null) continue;
            page.append(matches.get(type));
        }
        revalidate();
        repaint();
    }
}
