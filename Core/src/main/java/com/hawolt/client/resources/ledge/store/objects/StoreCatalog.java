package com.hawolt.client.resources.ledge.store.objects;

import org.json.JSONArray;
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
                storeItem.setTags(item.getJSONArray("tags"));
                JSONArray tags = storeItem.getTags();
                for (int i = 0; i < tags.length(); i++) {
                    Object o = tags.get(i);
                    if (!(o instanceof String tag)) continue;
                    if (tag.contains("skin_variant_skin")) {
                        String tmp = tag.replaceAll("[^0-9]", "");
                        if (!tmp.isEmpty()) {
                            long variantId = Long.parseLong(tmp);
                            storeItem.setVariantId(variantId);
                        }
                    } else if (tag.contains("skin_variant_bundle")) {
                        String tmp = tag.replaceAll("[^0-9]", "");
                        if (!tmp.isEmpty()) {
                            long variantBundleId = Long.parseLong(tag.replaceAll("[^0-9]", ""));
                            storeItem.setVariantBundleId(variantBundleId);
                        }
                    }
                }
                if (storeItem.getVariantBundleId() != 0L) {
                    JSONObject variantBundle = get(InventoryType.BUNDLES).get(storeItem.getVariantBundleId()).asJSON();
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
