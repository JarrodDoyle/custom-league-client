package com.hawolt.client.resources.ledge.store;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.client.resources.ledge.store.objects.StoreCatalog;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.ledge.store.objects.Wallet;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import com.hawolt.http.layer.impl.OkHttpResponse;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created: 28/07/2023 00:21
 * Author: Twitter @hawolt
 **/

public class StoreLedge extends AbstractLedgeEndpoint {
    public StoreLedge(LeagueClient client) {
        super(client);
    }

    public StoreCatalog catalogV1() throws IOException {
        if (client.isCached(CacheElement.STORE_CATALOG)) return client.getCachedValue(CacheElement.STORE_CATALOG);
        String uri = String.format("%s/%s/v%s/catalog?region=%s&language=en_GB",
                base,
                name(),
                "1",
                client.getVirtualLeagueClientInstance().getPlatform()
        );
        Request request = jsonRequest(uri)
                .get()
                .build();
        IResponse response = OkHttpResponse.from(request, gateway);
        JSONArray array = new JSONArray(response.asString());
        StoreCatalog catalog = new StoreCatalog();
        for (int i = 0; i < array.length(); i++) {
            StoreItem item = new StoreItem(new JSONArray().put(array.getJSONObject(i)));
            if (!catalog.containsKey(item.getInventoryType())) catalog.put(item.getInventoryType(), new HashMap<>());
            catalog.get(item.getInventoryType()).put(item.getItemId(), item);
        }
        catalog.bundle();
        return catalog;
    }

    public StoreItem lookupV1(InventoryType type, long itemId) throws IOException {
        String uri = String.format("%s/%s/v%s/catalog/itemlookup?language=en_GB",
                base,
                name(),
                "1"
        );
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("inventoryType", type.name());
        object.put("itemId", itemId);
        array.put(object);
        Request request = jsonRequest(uri)
                .post(RequestBody.create(array.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new StoreItem(new JSONArray(response.asString()));
    }

    public Wallet getBalanceV2() throws IOException {
        String uri = String.format("%s/%s/v%s/wallet",
                base,
                name(),
                "2"
        );
        Request request = jsonRequest(uri)
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new Wallet(new JSONObject(response.asString()));
    }

    @Override
    public int version() {
        return 0;
    }

    @Override
    public String name() {
        return "storefront";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-store";
    }

    @Override
    public String auth() {
        return String.join(" ", "Bearer", client.getVirtualLeagueClientInstance().getLeagueClientSupplier().getSimple("access_token"));
    }
}
