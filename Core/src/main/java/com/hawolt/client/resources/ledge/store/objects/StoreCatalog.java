package com.hawolt.client.resources.ledge.store.objects;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created: 29/09/2023 20:37
 * Author: Twitter @hawolt
 **/

public class StoreCatalog extends HashMap<InventoryType, HashMap<Long, StoreItem>> {
    public void bundle() {
        List<StoreItem> list = getList();
        for (StoreItem storeItem : list) {
            JSONObject item = storeItem.asJSON();
            if (item.has("tags")) {
                storeItem.setTags(item.getJSONArray("tags").toString());
                String tags = storeItem.getTags();
                if (tags.contains("skin_variant")) {
                    long variantId = Long.parseLong(tags.substring(45, 60).replaceAll("[^0-9]", ""));
                    storeItem.setVariantId(variantId);
                    long variantBundleId = Long.parseLong(tags.substring(75, 90).replaceAll("[^0-9]", ""));
                    storeItem.setVariantBundleId(variantBundleId);
                    JSONObject variantBundle = get(InventoryType.BUNDLES).get(variantBundleId).asJSON();
                    int bundleDiscountMinCost = variantBundle.getInt("bundleDiscountMinCost");
                    storeItem.setBundleDiscountMinCost(bundleDiscountMinCost);
                }
            }
        }
    }

    public List<StoreItem> getList() {
        return new ArrayList<>(values().stream().map(HashMap::values).flatMap(Collection::stream).toList());
    }
}
