package com.hawolt.client.resources.ledge.parties;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.AbstractLedgeEndpoint;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.parties.objects.PartiesRegistration;
import com.hawolt.client.resources.ledge.parties.objects.PartyException;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyAction;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyRole;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyType;
import com.hawolt.client.resources.ledge.parties.objects.data.PositionPreference;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import com.hawolt.virtual.leagueclient.client.Authentication;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created: 19/01/2023 16:04
 * Author: Twitter @hawolt
 **/

public class PartiesLedge extends AbstractLedgeEndpoint {

    public PartiesLedge(LeagueClient client) {
        super(client);
    }

    public String getGameClientVersion() {
        String leagueClientVersion = gameVersionSupplier.getVersionValue("League of Legends.exe");
        int majorIndex = leagueClientVersion.indexOf('.');
        String major = leagueClientVersion.substring(0, majorIndex);
        int majorChildIndex = leagueClientVersion.indexOf('.', majorIndex + 1);
        String majorChild = leagueClientVersion.substring(majorIndex + 1, majorChildIndex);
        String remainder = leagueClientVersion.substring(majorChildIndex + 1);
        int minorIndex = remainder.indexOf('.');
        String minor = remainder.substring(0, minorIndex);
        String minorChild = remainder.substring(minorIndex + 1);
        return String.format(
                "%s.%s.%s+branch.releases-%s-%s.code.public.content.release",
                major,
                majorChild,
                minor + String.format("%04d", Integer.parseInt(minorChild)),
                major,
                majorChild
        );
    }

    public String getCurrentPartyId() throws IOException {
        return getCurrentRegistration().getCurrentParty().getPartyId();
    }

    public PartiesRegistration register() throws IOException {
        JSONObject object = new JSONObject();
        object.put("accountId", userInformation.getOriginalAccountId());
        object.put("createdAt", 0L);
        object.put("currentParty", JSONObject.NULL);
        object.put("eligibilityHash", 0L);
        object.put("parties", JSONObject.NULL);
        object.put("platformId", platform.name());
        object.put("puuid", userInformation.getSub());
        JSONObject registration = new JSONObject();
        registration.put("gameClientVersion", getGameClientVersion());
        registration.put("inventoryToken", JSONObject.NULL);
        registration.put("inventoryTokens", new JSONArray());
        LedgeEndpoint ledge = client.getLedge();
        registration.put("rankedOverviewToken", ledge.getLeague().getRankedOverviewToken());
        registration.put("simpleInventoryToken", ledge.getInventoryService().getInventoryToken());
        registration.put("summonerToken", ledge.getSummoner().getSummonerToken());
        registration.put("userInfoToken", virtualLeagueClient.get(Authentication.USERINFO).getSimple("userinfo_token"));
        object.put("registration", registration);
        object.put("serverUtcMillis", 0L);
        object.put("summonerId", userInformation.getUserInformationLeagueAccount().getSummonerId());
        object.put("version", 0L);
        String uri = String.format("%s/%s/v%s/players/%s",
                base,
                name(),
                version(),
                userInformation.getSub()
        );
        Request request = new Request.Builder()
                .url(uri)
                .addHeader("Authorization", auth())
                .addHeader("User-Agent", agent())
                .addHeader("Accept", "application/json")
                .put(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject o = new JSONObject(response.asString());
        return updateAndReturnPartiesRegistration(new PartiesRegistration(o));
    }

    public PartiesRegistration invite(Summoner summoner) throws IOException, PartyException {
        return invite(getCurrentRegistration(), summoner.getPUUID());
    }

    public PartiesRegistration invite(PartiesRegistration registration, Summoner summoner) throws IOException, PartyException {
        return invite(registration, summoner.getPUUID());
    }

    public PartiesRegistration invite(Summoner... summoners) throws IOException, PartyException {
        return invite(getCurrentRegistration(), Arrays.stream(summoners).map(Summoner::getPUUID).toArray(String[]::new));
    }

    public PartiesRegistration invite(PartiesRegistration registration, Summoner... summoners) throws IOException, PartyException {
        return invite(Arrays.stream(summoners).map(Summoner::getPUUID).toArray(String[]::new));
    }

    public PartiesRegistration invite(String... puuids) throws IOException, PartyException {
        return invite(getCurrentRegistration(), puuids);
    }

    public PartiesRegistration invite(PartiesRegistration registration, String... puuids) throws IOException, PartyException {
        if (registration == null) throw new PartyException();
        String uri = String.format("%s/%s/v%s/parties/%s/invite",
                base,
                name(),
                version(),
                getCurrentRegistration().getCurrentParty().getPartyId()
        );
        JSONArray array = new JSONArray();
        for (String puuid : puuids) {
            array.put(puuid);
        }
        Request request = new Request.Builder()
                .url(uri)
                .addHeader("Authorization", auth())
                .addHeader("User-Agent", agent())
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(array.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject o = new JSONObject(response.asString());
        return updateAndReturnPartiesRegistration(new PartiesRegistration(o));
    }

    public PartiesRegistration gamemode(String partyId, long maxPartySize, long maxTeamSize, long queueId) throws IOException {
        String uri = String.format("%s/%s/v%s/parties/%s/gamemode",
                base,
                name(),
                version(),
                partyId
        );
        JSONObject object = new JSONObject();
        object.put("allowSpectators", JSONObject.NULL);
        object.put("botDifficulty", JSONObject.NULL);
        object.put("customsSettings", JSONObject.NULL);
        object.put("gameCustomization", new JSONObject());
        object.put("gameType", "");
        object.put("gameTypeConfigId", JSONObject.NULL);
        object.put("mapId", JSONObject.NULL);
        object.put("maxPartySize", maxPartySize);
        object.put("maxTeamSize", maxTeamSize);
        object.put("queueId", queueId);
        Request request = jsonRequest(uri).put(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject o = new JSONObject(response.asString());
        return updateAndReturnPartiesRegistration(new PartiesRegistration(o));
    }

    public PartiesRegistration partytype(PartyType type) throws IOException {
        String uri = String.format("%s/%s/v%s/parties/%s/partytype",
                base,
                name(),
                version(),
                getCurrentPartyId()
        );
        Request request = jsonRequest(uri).put(RequestBody.create(type.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject o = new JSONObject(response.asString());
        return updateAndReturnPartiesRegistration(new PartiesRegistration(o));
    }

    public PartiesRegistration role(PartyRole role) throws IOException {
        return role(getCurrentPartyId(), role);
    }

    public PartiesRegistration role(String partyId, PartyRole role) throws IOException {
        // make sure registration is present
        PartiesRegistration registration = getCurrentRegistration();
        String uri = String.format("%s/%s/v%s/parties/%s/members/%s/role",
                base,
                name(),
                version(),
                partyId,
                userInformation.getSub()
        );
        Request request = jsonRequest(uri)
                .put(RequestBody.create(role.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject o = new JSONObject(response.asString());
        return updateAndReturnPartiesRegistration(new PartiesRegistration(o));
    }

    public JSONObject setQueueAction(PartyAction action) throws IOException {
        String uri = String.format("%s/%s/v%s/parties/%s/members/%s/%s",
                base,
                name(),
                version(),
                getCurrentPartyId(),
                userInformation.getSub(),
                action.name().toLowerCase() + "Action"
        );
        Request request = jsonRequest(uri)
                .post(RequestBody.create(new byte[0], Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject reference = new JSONObject(response.asString());
        if (!reference.has("errorCode")) {
            updateAndReturnPartiesRegistration(new PartiesRegistration(reference));
        }
        return reference;
    }

    public PartiesRegistration metadata(PositionPreference first, PositionPreference second) throws IOException {
        String uri = String.format("%s/%s/v%s/parties/%s/members/%s/metadata",
                base,
                name(),
                version(),
                getCurrentPartyId(),
                userInformation.getSub()
        );
        JSONObject object = new JSONObject();
        object.put("championSelection", JSONObject.NULL);
        object.put("properties", JSONObject.NULL);
        object.put("skinSelection", JSONObject.NULL);
        JSONArray array = new JSONArray();
        array.put(first.name());
        array.put(second.name());
        object.put("positionPref", array);
        Request request = jsonRequest(uri)
                .put(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject o = new JSONObject(response.asString());
        return updateAndReturnPartiesRegistration(new PartiesRegistration(o));
    }

    public PartiesRegistration ready() throws IOException {
        String uri = String.format("%s/%s/v%s/parties/%s/members/%s/ready",
                base,
                name(),
                version(),
                getCurrentRegistration().getCurrentParty().getPartyId(),
                userInformation.getSub()
        );
        Request request = jsonRequest(uri)
                .put(RequestBody.create("true", Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject o = new JSONObject(response.asString());
        return updateAndReturnPartiesRegistration(new PartiesRegistration(o));
    }

    public PartiesRegistration resume() throws IOException {
        String uri = String.format("%s/%s/v%s/parties/%s/members/%s/resumeAction",
                base,
                name(),
                version(),
                getCurrentRegistration().getCurrentParty().getPartyId(),
                userInformation.getSub()
        );
        Request request = jsonRequest(uri)
                .post(RequestBody.create(new byte[0], Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject o = new JSONObject(response.asString());
        return updateAndReturnPartiesRegistration(new PartiesRegistration(o));
    }

    public PartiesRegistration getOwnPlayer() throws IOException {
        String uri = String.format("%s/%s/v%s/players/%s",
                base,
                name(),
                version(),
                client.getVirtualRiotClient().getRiotClientUser().getPUUID()
        );
        Request request = jsonRequest(uri)
                .get()
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        JSONObject o = new JSONObject(response.asString());
        return updateAndReturnPartiesRegistration(new PartiesRegistration(o));
    }

    public boolean enteredGame() throws IOException {
        String uri = String.format("%s/%s/v%s/parties/%s/members/%s/enteredGame",
                base,
                name(),
                version(),
                getCurrentRegistration().getCurrentParty().getPartyId(),
                client.getVirtualRiotClient().getRiotClientUser().getPUUID()
        );
        Request request = jsonRequest(uri)
                .post(RequestBody.create(new byte[0], Constant.APPLICATION_JSON))
                .build();
        IResponse response = OkHttp3Client.execute(request, gateway);
        return response.code() == 200;
    }

    private PartiesRegistration updateAndReturnPartiesRegistration(PartiesRegistration registration) {
        client.cache(CacheElement.PARTY_REGISTRATION, registration);
        return registration;
    }

    public PartiesRegistration getCurrentRegistration() {
        if (client.isCached(CacheElement.PARTY_REGISTRATION)) {
            return client.getCachedValue(CacheElement.PARTY_REGISTRATION);
        } else {
            try {
                return updateAndReturnPartiesRegistration(register());
            } catch (IOException ex) {
                Logger.fatal("Unable to fetch registration, exiting");
                System.exit(1);
                return null;
            }
        }
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "parties-ledge";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-lobby";
    }

}
