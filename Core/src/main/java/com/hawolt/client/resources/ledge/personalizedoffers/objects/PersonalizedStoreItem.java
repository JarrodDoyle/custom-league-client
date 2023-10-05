package com.hawolt.client.resources.ledge.personalizedoffers.objects;

import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.logger.Logger;
import org.json.JSONObject;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class PersonalizedStoreItem {
    private int spotIndex, discountAmount, discountPrice, originalPrice;
    private InventoryType inventoryType;
    private String offerId, name;
    private JSONObject object;
    private boolean owned;
    private long itemId;

    public PersonalizedStoreItem(JSONObject object) {
        this.itemId = object.getLong("itemId");
        this.offerId = object.getString("offerId");
        this.name = object.getString("name");
        this.inventoryType = InventoryType.valueOf(object.getString("inventoryType"));
        this.discountAmount = object.getInt("discountAmount");
        this.discountPrice = object.getInt("discountPrice");
        this.originalPrice = object.getInt("originalPrice");
        this.spotIndex = object.getInt("spotIndex");
        this.owned = object.getBoolean("owned");
        this.object = object;
    }

    public JSONObject getObject() {
        return object;
    }

    public long getItemId() {
        return itemId;
    }

    public String getOfferId() {
        return offerId;
    }

    public String getName() {
        return name;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
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

    public int getSpotIndex() {
        return spotIndex;
    }

    public boolean isOwned() {
        return owned;
    }

    @Override
    public String toString() {
        return "PersonalizedStoreItem{" +
                "spotIndex=" + spotIndex +
                ", discountAmount=" + discountAmount +
                ", discountPrice=" + discountPrice +
                ", originalPrice=" + originalPrice +
                ", inventoryType=" + inventoryType +
                ", owned=" + owned +
                ", offerId='" + offerId + '\'' +
                ", name='" + name + '\'' +
                ", object=" + object +
                ", itemId=" + itemId +
                '}';
    }
}
