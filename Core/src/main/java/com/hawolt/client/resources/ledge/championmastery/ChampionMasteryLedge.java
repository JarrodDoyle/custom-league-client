package com.hawolt.client.resources.ledge.championmastery;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;

import java.io.IOException;

public class ChampionMasteryLedge extends AbstractLedgeEndpoint {
    public ChampionMasteryLedge(LeagueClient client) {
        super(client);
    }

    public JSONArray getMasteryLevels() throws IOException {
        String uri = String.format("%s/%s/player/%s/champions",
                base,
                name(),
                client.getCachedValue(CacheElement.SUMMONER_ID)
        );
        Request request = jsonRequest(uri)
                .post(RequestBody.create("\"" + client.getLedge().getSummoner().getSummonerToken() + "\"", Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new JSONArray(response.asString());
    }


    @Override
    public int version() {
        return 0;
    }

    @Override
    public String name() {
        return "championmastery-ledge";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-collections";
    }
}
