package com.hawolt.client.resources.ledge.personalizedoffers.objects;

import com.hawolt.client.resources.ledge.store.objects.InventoryType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PersonalizedOfferItem {
    private int spotIndex, discountAmount, discountPrice, originalPrice;
    private Map<Integer, ArrayList<String>> offerIds = new HashMap<>();
    private Map<Integer, ArrayList<Boolean>> owned = new HashMap<>();
    private Map<Integer, ArrayList<Long>> itemIds = new HashMap<>();
    private InventoryType inventoryType;
    private String name;

    public PersonalizedOfferItem(PersonalizedStoreItem champion, PersonalizedStoreItem skin) {
        this.spotIndex = champion.getSpotIndex();
        this.discountAmount = champion.getDiscountAmount();
        this.name = skin.getName();
        this.inventoryType = skin.getInventoryType();
        if (champion.isOwned() && skin.isOwned()) {
            this.discountPrice = 0;
        } else if (champion.isOwned()) {
            this.originalPrice = skin.getOriginalPrice();
            this.discountPrice = skin.getDiscountPrice();
        } else {
            this.originalPrice = champion.getOriginalPrice() + skin.getOriginalPrice();
            this.discountPrice = champion.getDiscountPrice() + skin.getDiscountPrice();
        }
        owned.put(this.spotIndex, new ArrayList<>());
        owned.get(this.spotIndex).add(champion.isOwned());
        owned.get(this.spotIndex).add(skin.isOwned());
        itemIds.put(this.spotIndex, new ArrayList<>());
        itemIds.get(this.spotIndex).add(champion.getItemId());
        itemIds.get(this.spotIndex).add(skin.getItemId());
        offerIds.put(this.spotIndex, new ArrayList<>());
        offerIds.get(this.spotIndex).add(champion.getOfferId());
        offerIds.get(this.spotIndex).add(skin.getOfferId());
    }

    public int getSpotIndex() {
        return spotIndex;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public int getOriginalPrice() {
        return originalPrice;
    }

    public Map<Integer, ArrayList<String>> getOfferIds() {
        return offerIds;
    }

    public Map<Integer, ArrayList<Long>> getItemIds() {
        return itemIds;
    }

    public Map<Integer, ArrayList<Boolean>> getOwned() {
        return owned;
    }

    public long getChampionId() {
        return itemIds.get(spotIndex).get(0);
    }

    public long getSkinId() {
        return itemIds.get(spotIndex).get(1);
    }

    public String getName() {
        return name;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

}
