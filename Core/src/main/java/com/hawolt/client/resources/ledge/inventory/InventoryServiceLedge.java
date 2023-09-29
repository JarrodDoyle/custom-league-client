package com.hawolt.client.resources.ledge.inventory;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created: 19/01/2023 16:38
 * Author: Twitter @hawolt
 **/

public class InventoryServiceLedge extends AbstractLedgeEndpoint {
    public InventoryServiceLedge(LeagueClient client) {
        super(client);
    }

    public String getInventoryToken() throws IOException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("inventories")
                .addPathSegment("simple")
                .addQueryParameter("puuid", userInformation.getSub())
                .addQueryParameter("location", String.format("lolriot.ams1.%s", platform.name().toLowerCase()))
                .addQueryParameter("inventoryTypes", "CHAMPION")
                .addQueryParameter("inventoryTypes", "CHAMPION_SKIN")
                .addQueryParameter("inventoryTypes", "COMPANION")
                .addQueryParameter("inventoryTypes", "TFT_MAP_SKIN")
                .addQueryParameter("inventoryTypes", "EVENT_PASS")
                .addQueryParameter("inventoryTypes", "BOOST")
                .addQueryParameter("accountId", String.valueOf(client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeague().getCUID()))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", auth())
                .addHeader("User-Agent", agent())
                .addHeader("Accept", "application/json")
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        String plain = response.asString();
        JSONObject object = new JSONObject(plain);
        JSONObject data = object.getJSONObject("data");
        return data.getString("itemsJwt");
    }

    public JSONObject getBalances() throws IOException {
        String uri = String.format("%s/%s/v%s/walletsbalances",
                base,
                name(),
                version()
        );
        Request request = new Request.Builder()
                .url(uri)
                .addHeader("Authorization", auth())
                .addHeader("User-Agent", agent())
                .addHeader("Accept", "application/json")
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new JSONObject(response.asString());
    }

    public String getInventoryJwt(String type) throws IOException {
        HttpUrl uri = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v2")
                .addPathSegment("inventoriesWithLoyalty")
                .addQueryParameter("puuid", userInformation.getSub())
                .addQueryParameter("location", String.format("lolriot.ams1.%s", platform.name().toLowerCase()))
                .addQueryParameter("accountId", String.valueOf(client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeague().getCUID()))
                .addQueryParameter("inventoryTypes", type)
                .addQueryParameter("signed", "true")
                .build();
        Request request = new Request.Builder()
                .url(uri)
                .addHeader("Authorization", auth())
                .addHeader("User-Agent", agent())
                .addHeader("Accept", "application/json")
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new JSONObject(response.asString()).getJSONObject("data").get("itemsJwt").toString();
    }

    public String getLegendInstanceId(int itemId) throws IOException {
        HttpUrl uri = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v2")
                .addPathSegment("inventoriesWithLoyalty")
                .addQueryParameter("puuid", userInformation.getSub())
                .addQueryParameter("location", String.format("lolriot.ams1.%s", platform.name().toLowerCase()))
                .addQueryParameter("accountId", String.valueOf(client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeague().getCUID()))
                .addQueryParameter("inventoryTypes", "TFT_PLAYBOOK")
                .build();
        Request request = new Request.Builder()
                .url(uri)
                .addHeader("Authorization", auth())
                .addHeader("User-Agent", agent())
                .addHeader("Accept", "application/json")
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        String plain = response.asString();
        JSONObject object = new JSONObject(plain);
        HashMap<String, Object> map = (HashMap<String, Object>) object.toMap();
        HashMap<String, Object> data = (HashMap<String, Object>) map.get("data");
        HashMap<String, Object> items = (HashMap<String, Object>) data.get("items");
        ArrayList<HashMap<String, Object>> tftPlaybook = (ArrayList<HashMap<String, Object>>) items.get("TFT_PLAYBOOK");
        for (int i = 0; i < tftPlaybook.size(); i++) {
            int playbookItemId = (int) tftPlaybook.get(i).get("itemId");
            if (itemId == playbookItemId) {
                return (String) tftPlaybook.get(i).get("instanceId");
            }
        }
        return null;
    }


    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "lolinventoryservice-ledge";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-inventory";
    }
}
