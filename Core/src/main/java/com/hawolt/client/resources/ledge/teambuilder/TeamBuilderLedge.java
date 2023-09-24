package com.hawolt.client.resources.ledge.teambuilder;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.client.resources.ledge.teambuilder.objects.MatchContext;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 19/01/2023 16:04
 * Author: Twitter @hawolt
 **/

public class TeamBuilderLedge extends AbstractLedgeEndpoint {

    public TeamBuilderLedge(LeagueClient client) {
        super(client);
    }

    public MatchContext indicateAfkReadiness() throws IOException {
        String uri = String.format("%s/%s/v%s/indicateAfkReadiness/accountId/%s/summonerId/%s",
                base,
                name(),
                version(),
                client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeague().getCUID(),
                client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId()
        );
        JSONObject object = new JSONObject();
        object.put("additionalInventoryJwt", "");
        object.put("afkReady", true);
        object.put("initialSpellIds", new JSONArray().put(14).put(4));
        object.put("lastSelectedSkinIdByChampionId", new JSONObject());
        object.put("simplifiedInventoryJwt", client.getLedge().getInventoryService().getInventoryToken());
        Request request = jsonRequest(uri)
                .post(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        MatchContext context = new MatchContext(new JSONObject(response.asString()));
        client.cache(CacheElement.MATCH_CONTEXT, context);
        return context;
    }

    public IResponse acceptPickOrderSwap(int swapId) throws IOException {
        return handlePickOrderSwap("acceptPickOrderSwap", swapId);
    }

    public IResponse declinePickOrderSwap(int swapId) throws IOException {
        return handlePickOrderSwap("declinePickOrderSwap", swapId);
    }

    private IResponse handlePickOrderSwap(String type, int swapId) throws IOException {
        String uri = String.format("%s/%s/v%s/%s/accountId/%s/summonerId/%s",
                base,
                name(),
                "1",
                type,
                client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeague().getCUID(),
                client.getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId()
        );
        JSONObject object = new JSONObject();
        object.put("swapId", swapId);
        Request request = jsonRequest(uri)
                .post(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        return OkHttp3Client.execute(request, gateway);
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String name() {
        return "team-builder-ledge";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-lobby-team-builder";
    }

}
