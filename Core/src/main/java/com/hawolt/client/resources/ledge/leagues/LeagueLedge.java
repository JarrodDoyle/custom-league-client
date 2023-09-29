package com.hawolt.client.resources.ledge.leagues;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.client.resources.ledge.leagues.objects.LeagueLedgeNotifications;
import com.hawolt.client.resources.ledge.leagues.objects.RankedStatistic;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import okhttp3.Request;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 19/01/2023 16:38
 * Author: Twitter @hawolt
 **/

public class LeagueLedge extends AbstractLedgeEndpoint {
    public LeagueLedge(LeagueClient client) {
        super(client);
    }

    public String getRankedOverviewToken() throws IOException {
        String uri = String.format("%s/%s/v%s/signedRankedStats",
                base,
                name(),
                version()
        );
        Request request = jsonRequest(uri).get().build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new JSONObject(response.asString()).getString("jwt");
    }

    public RankedStatistic getRankedStats(String puuid) throws IOException {
        String uri = String.format("%s/%s/v%s/rankedStats/puuid/%s",
                base,
                name(),
                version(),
                puuid
        );
        Request request = jsonRequest(uri).get().build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new RankedStatistic(new JSONObject(response.asString()));
    }

    public JSONObject getOwnRankedStats() throws IOException {
        String uri = String.format("%s/%s/v%s/signedRankedStats",
                base,
                name(),
                version()
        );
        Request request = jsonRequest(uri).get().build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return new JSONObject(response.asString());
    }

    public LeagueLedgeNotifications getNotifications() throws IOException {
        String uri = String.format("%s/%s/v%s/notifications",
                base,
                name(),
                version()
        );
        Request request = jsonRequest(uri).get().build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        LeagueLedgeNotifications notifications = new LeagueLedgeNotifications(new JSONObject(response.asString()));
        client.cache(CacheElement.LEAGUE_LEDGE_NOTIFICATION, notifications);
        return notifications;
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String name() {
        return "leagues-ledge";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-ranked";
    }
}
