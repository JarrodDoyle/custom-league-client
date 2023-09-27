package com.hawolt.client.resources.ledge.personalizedoffers;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 28/07/2023 00:21
 * Author: Twitter @hawolt
 **/

public class PersonalizedOffersLedge extends AbstractLedgeEndpoint {
    public PersonalizedOffersLedge(LeagueClient client) {
        super(client);
    }

    public JSONObject accept(String offerId) throws IOException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("player")
                .addPathSegment("offers")
                .addPathSegment("accept")
                .addQueryParameter("lang", "en_GB")
                .build();
        JSONObject object = new JSONObject();
        JSONArray offers = new JSONArray();
        offers.put(new JSONObject().put("offerId", offerId));
        object.put("offers", offers);
        Request request = jsonRequest(url)
                .post(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new JSONObject(response.asString());
    }

    public JSONObject view(String... offerIds) throws IOException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("player")
                .addPathSegment("offers")
                .addPathSegment("view")
                .addQueryParameter("lang", "en_GB")
                .build();
        JSONObject object = new JSONObject();
        JSONArray offers = new JSONArray();
        for (String offerId : offerIds) {
            offers.put(new JSONObject().put("offerId", offerId));
        }
        object.put("offers", offers);
        Request request = jsonRequest(url)
                .post(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new JSONObject(response.asString());
    }

    public JSONObject offers() throws IOException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(base.substring(base.lastIndexOf('/') + 1))
                .addPathSegment(name())
                .addPathSegment("v" + version())
                .addPathSegment("player")
                .addPathSegment("offers")
                .addQueryParameter("lang", "en_GB")
                .build();
        Request request = jsonRequest(url)
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new JSONObject(response.asString());
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "personalized-offers";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-yourshop";
    }

    @Override
    public String auth() {
        return String.join(" ", "Bearer", client.getVirtualLeagueClientInstance().getLeagueClientSupplier().getSimple("access_token"));
    }
}
