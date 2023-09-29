package com.hawolt.client.resources.ledge.store.objects;

import com.hawolt.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created: 28/07/2023 01:58
 * Author: Twitter @hawolt
 **/

public class StoreItem {
    private final List<Price> prices = new ArrayList<>();
    private int discountCostBE, discountCostRP, bundleDiscountMinCost;
    private String offerId, name, description, tags;
    private long itemId, variantId, variantBundleId;
    private SubInventoryType subInventoryType;
    private float discountBE, discountRP;
    private boolean active, variantOwned;
    private InventoryType inventoryType;
    private JSONObject object;
    private Date releaseDate;

    public StoreItem(JSONArray array) {
        JSONObject item = array.getJSONObject(0);
        this.itemId = item.getLong("itemId");
        if (item.has("offerId")) this.offerId = item.getString("offerId");
        this.inventoryType = InventoryType.valueOf(item.getString("inventoryType"));
        if (item.has("subInventoryType")) {
            this.subInventoryType = SubInventoryType.valueOf(item.getString("subInventoryType").toUpperCase());
        }
        this.active = item.getBoolean("active");
        JSONArray prices = item.getJSONArray("prices");
        for (int i = 0; i < prices.length(); i++) {
            this.prices.add(new Price(prices.getJSONObject(i)));
        }
        if (item.has("bundleDiscountMinCost")) this.bundleDiscountMinCost = item.getInt("bundleDiscountMinCost");
        if (item.has("sale")) {
            JSONObject sale = item.getJSONObject("sale");
            if (sale.has("prices")) {
                JSONArray salePrices = sale.getJSONArray("prices");
                for (int i = 0; i < salePrices.length(); i++) {
                    JSONObject salePrice = salePrices.getJSONObject(i);
                    if (salePrice.getString("currency").equals("IP")) {
                        this.discountCostBE = salePrice.getInt("cost");
                        if (!salePrice.has("discount")) continue;
                        if (salePrice.getFloat("discount") == 0) continue;
                        this.discountBE = salePrice.getFloat("discount");
                    }
                    if (salePrice.getString("currency").equals("RP")) {
                        this.discountCostRP = salePrice.getInt("cost");
                        if (!salePrice.has("discount")) continue;
                        if (salePrice.getFloat("discount") == 0) continue;
                        this.discountRP = salePrice.getFloat("discount");
                    }
                }
            }
        }
        if (item.has("localizations")) {
            JSONObject localizations = item.getJSONObject("localizations");
            if (localizations.has("en_GB")) {
                JSONObject enGB = localizations.getJSONObject("en_GB");
                if (enGB.has("description")) this.description = enGB.getString("description");
                if (enGB.has("name")) this.name = enGB.getString("name");
            }
        }
        String releaseDate = item.getString("releaseDate");
        try {
            this.releaseDate = Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(releaseDate)));
        } catch (DateTimeParseException e) {
            Logger.error("Could not parse release date '{}': {}", releaseDate, e);
        }
        this.object = item;
    }

    public JSONObject asJSON() {
        return object;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isRiotPointPurchaseAvailable() {
        return getCorrectRiotPointCost() > 0 || prices.stream().anyMatch(price -> price.getCurrency().equals("RP"));
    }

    public int getRiotPointCost() {
        return prices.stream().filter(price -> price.getCurrency().equals("RP")).mapToInt(Price::getCost).sum();
    }

    public boolean hasDiscount() {
        return hasDiscountBE() || hasDiscountRP();
    }

    public boolean hasDiscountBE() {
        return discountBE != 0 || discountCostBE > 0;
    }

    public boolean hasDiscountRP() {
        return discountRP != 0 || discountCostRP > 0;
    }

    public float getDiscountBE() {
        return discountBE;
    }

    public float getDiscountRP() {
        return discountRP;
    }

    public int getCorrectBlueEssenceCost() {
        if (hasDiscountBE()) {
            return discountCostBE;
        } else {
            return getBlueEssenceCost();
        }
    }

    public int getCorrectRiotPointCost() {
        if (ownVariantId()) {
            return bundleDiscountMinCost;
        }
        if (hasDiscountRP()) {
            return discountCostRP;
        } else {
            return getRiotPointCost();
        }
    }

    public boolean isBlueEssencePurchaseAvailable() {
        return getCorrectBlueEssenceCost() > 0 || prices.stream().anyMatch(price -> price.getCurrency().equals("IP"));
    }

    public int getBlueEssenceCost() {
        return prices.stream().filter(price -> price.getCurrency().equals("IP")).mapToInt(Price::getCost).sum();
    }

    public int getPointCost() {
        return (int) Math.ceil(getBlueEssenceCost() / 450D);
    }

    public List<Price> getPrices() {
        return prices;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public boolean isBundle() {
        return inventoryType.equals(InventoryType.BUNDLES);
    }

    public boolean isSkinBundle() {
        if (subInventoryType != null)
            return subInventoryType.equals(SubInventoryType.SKIN_BUNDLE);
        return false;
    }

    public boolean isSkinVariantBundle() {
        if (subInventoryType != null)
            return subInventoryType.equals(SubInventoryType.SKIN_VARIANT_BUNDLE);
        return false;
    }

    public boolean isChroma() {
        if (subInventoryType != null) {
            return subInventoryType.equals(SubInventoryType.RECOLOR);
        }
        return false;
    }

    public boolean isChromaBundle() {
        if (subInventoryType != null) {
            return subInventoryType.equals(SubInventoryType.CHROMA_BUNDLE);
        }
        return false;
    }

    public boolean isTFTMapSkin() {
        if (inventoryType != null) {
            return inventoryType.equals(InventoryType.TFT_MAP_SKIN);
        }
        return false;
    }

    public boolean isChibiCompanion(JSONObject item) {
        if (item.has("bundled")) {
            JSONObject bundled = item.getJSONObject("bundled");
            if (bundled.has("items")) {
                JSONArray items = bundled.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject bundledItem = items.getJSONObject(i);
                    if (bundledItem.has("inventoryType")) {
                        if (bundledItem.get("inventoryType").equals(InventoryType.COMPANION.name())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void setBundleDiscountMinCost(int bundleDiscountMinCost) {
        this.bundleDiscountMinCost = bundleDiscountMinCost;
    }

    public SubInventoryType getSubInventoryType() {
        return subInventoryType;
    }

    public boolean isActive() {
        return active;
    }

    public long getItemId() {
        return itemId;
    }

    public void setVariantOwned(boolean value) {
        this.variantOwned = value;
    }

    public long getVariantId() {
        return variantId;
    }

    public void setVariantId(long variantId) {
        this.variantId = variantId;
    }

    public long getVariantBundleId() {
        return variantBundleId;
    }

    public void setVariantBundleId(long variantBundleId) {
        this.variantBundleId = variantBundleId;
    }

    public boolean hasVariantId() {
        return variantId != 0;
    }

    public boolean ownVariantId() {
        return variantOwned;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String toString() {
        return "StoreItem{" +
                "list=" + prices +
                ", offerId='" + offerId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", inventoryType=" + inventoryType +
                ", subInventoryType=" + subInventoryType +
                ", active=" + active +
                ", object=" + object +
                ", itemId=" + itemId +
                ", releaseDate=" + releaseDate +
                '}';
    }
}