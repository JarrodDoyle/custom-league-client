package com.hawolt.client.resources.ledge.gsm;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import okhttp3.Request;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 22/08/2023 16:48
 * Author: Twitter @hawolt
 **/

public class GameServiceMessageLedge extends AbstractLedgeEndpoint {
    public GameServiceMessageLedge(LeagueClient client) {
        super(client);
    }

    public JSONObject getCurrentGameInformation() throws IOException {
        String uri = String.format("%s/%s/v%s/ledge/region/%s/puuid/%s",
                base,
                name(),
                version(),
                platform.name(),
                userInformation.getSub()
        );
        Request request = jsonRequest(uri)
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request);
        return new JSONObject(response.asString());
    }

    public JSONObject getGameInfoByGameId(String gameId) throws IOException {
        String uri = String.format("%s/%s/v%s/ledge/games/shardId/%s/gameId/%s",
                base,
                name(),
                version(),
                platform.name(),
                gameId
        );
        Request request = jsonRequest(uri)
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request);
        return new JSONObject(response.asString());
    }
    
    public int deleteGame() throws IOException {
        String uri = String.format("%s/%s/v%s/ledge/region/%s/puuid/%s",
                base,
                name(),
                version(),
                platform.name(),
                userInformation.getSub()
        );
        Request request = jsonRequest(uri)
                .delete()
                .build();
        IResponse response = OkHttp3Client.execute(request);
        return response.code();
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "gsm";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-gameflow";
    }
}
