package com.hawolt.async.presence;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.client.misc.MapQueueId;
import com.hawolt.client.resources.ledge.gsm.GameServiceMessageLedge;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.CurrentParty;
import com.hawolt.client.resources.ledge.parties.objects.PartiesRegistration;
import com.hawolt.client.resources.ledge.parties.objects.PartyGameMode;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.impl.payload.RiotMessageMessagePayload;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.utility.Base64GZIP;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.xmpp.event.objects.presence.Presence;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created: 11/09/2023 17:08
 * Author: Twitter @hawolt
 **/

public class PresenceManager implements PacketCallback, IServiceMessageListener<RiotMessageServiceMessage> {
    private final LeagueClientUI leagueClientUI;
    private final LeagueClient leagueClient;

    public PresenceManager(LeagueClientUI leagueClientUI) {
        this.leagueClientUI = leagueClientUI;
        this.leagueClient = leagueClientUI.getLeagueClient();
    }

    private int getPresenceTierId(String tier) {
        return switch (tier) {
            case "IRON" -> 0;
            case "BRONZE" -> 1;
            case "SILVER" -> 2;
            case "GOLD" -> 3;
            case "PLATINUM" -> 4;
            case "EMERALD" -> 5;
            case "DIAMOND" -> 6;
            case "MASTER" -> 7;
            case "GRANDMASTER" -> 8;
            case "CHALLENGER" -> 9;
            default -> 0;
        };
    }

    private int getPresenceRankId(String rank) {
        return switch (rank) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            default -> 4;
        };
    }

    private Presence.Builder configure() throws Exception {
        JSONObject summonerRank = leagueClient.getLedge().getLeague().getOwnRankedStats();
        JSONArray queues = summonerRank.getJSONArray("queues");
        int rank = 0, tier = 0, lp = 0, target = 0;
        for (int i = 0; i < queues.length(); i++) {
            int temporaryTier;
            if (!queues.getJSONObject(i).has("tier")) continue;
            temporaryTier = getPresenceTierId(queues.getJSONObject(i).getString("tier"));
            if (temporaryTier > rank) {
                rank = temporaryTier;
                target = i;
            } else if (temporaryTier == rank) {
                int temporaryRank = getPresenceRankId(queues.getJSONObject(i).getString("rank"));
                if (temporaryRank < tier) {
                    tier = temporaryRank;
                    target = i;
                } else if (temporaryRank == tier) {
                    int leaguePoints = queues.getJSONObject(i).getInt("leaguePoints");
                    if (leaguePoints > lp) {
                        lp = leaguePoints;
                        target = i;
                    }
                }
            }
        }
        JSONArray masteryValues = leagueClient.getLedge().getChampionMastery().getMasteryLevels();
        long masteryLevel = 0;
        for (int i = 0; i < masteryValues.length(); i++) {
            masteryLevel = masteryLevel + masteryValues.getJSONObject(i).getInt("championLevel");
        }
        String puuid = leagueClient.getCachedValue(CacheType.PUUID);
        Summoner summoner = leagueClient.getLedge().getSummoner().resolveSummonerByPUUD(puuid);
        JSONObject presenceRank = queues.getJSONObject(target);
        Presence.Builder builder = new Presence.Builder();
        builder.setRankedLeagueQueue(presenceRank.getString("queueType"));
        builder.setRankedLeagueTier(presenceRank.has("tier") ? presenceRank.getString("tier") : "");
        builder.setRankedLeagueDivision(presenceRank.has("rank") ? presenceRank.getString("rank") : "NA");
        builder.setPuuid(puuid);
        builder.setGameStatus("outOfGame");
        builder.setLevel(String.valueOf(summoner.getLevel()));
        builder.setProfileIcon(String.valueOf(summoner.getProfileIconId()));
        builder.setMasteryScore(String.valueOf(masteryLevel));
        JSONObject challengeValues = leagueClient.getLedge().getChallenge().getChallengePoints();
        int currentChallengePoints = challengeValues.getInt("current");
        builder.setChallengePoints(String.valueOf(currentChallengePoints));
        String currentChallengeLevel = challengeValues.getString("level");
        builder.setChallengeCrystalLevel(currentChallengeLevel);
        return builder;
    }

    public void setIdlePresence() {
        Logger.error("IDLE");
        leagueClient.getCachedValueOrElse(CacheType.PRESENCE_BUILDER, this::configure, Logger::error).ifPresent(builder -> {
            builder.setGameStatus("outOfGame");
            builder.setQueueId("");
            builder.setGameMode("");
            builder.setGameQueueType("");
            builder.setPTY("");
            builder.setTimestamp("");
            builder.setIsObservable("");
            builder.setMapId("");
            builder.setChampionId("");
            builder.setIconOverride("");
            String status = leagueClientUI.getHeader().getChatStatus();
            set("default".equals(status) ? "chat" : status, builder.build());
        });
    }

    private void set(String status, Presence presence) {
        leagueClientUI.getLeagueClient().getXMPPClient().setCustomPresence(
                status, leagueClientUI.getLeagueClient().getCachedValue(CacheType.CHAT_STATUS), presence
        );
    }

    @Override
    public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) {
        Logger.error("CHAMPSELECT");
        if (typedObject == null || !typedObject.containsKey("data")) return;
        TypedObject data = typedObject.getTypedObject("data");
        if (data == null || !data.containsKey("flex.messaging.messages.AsyncMessage")) return;
        TypedObject message = data.getTypedObject("flex.messaging.messages.AsyncMessage");
        if (message == null || !message.containsKey("body")) return;
        TypedObject body = message.getTypedObject("body");
        if (body == null || !body.containsKey("com.riotgames.platform.serviceproxy.dispatch.LcdsServiceProxyResponse")) {
            return;
        }
        TypedObject response = body.getTypedObject("com.riotgames.platform.serviceproxy.dispatch.LcdsServiceProxyResponse");
        if (response == null || !response.containsKey("payload")) return;
        try {
            Object object = response.get("payload");
            if (object == null) return;
            JSONObject payload = new JSONObject(Base64GZIP.unzipBase64(object.toString()));
            int queueId = payload.getInt("queueId");
            Presence.Builder builder = leagueClient.getCachedValue(CacheType.PRESENCE_BUILDER);
            builder.setGameQueueType(MapQueueId.getGameQueueType(queueId));
            builder.setGameMode(MapQueueId.getGameMode(queueId));
            builder.setQueueId(String.valueOf(queueId));
            builder.setGameStatus("championSelect");
            builder.setMapId(MapQueueId.getMapId(queueId));
            String status = leagueClientUI.getLayoutManager().getHeader().getProfile().getSummoner().getStatus().getBoxStatus();
            if (status.equals("default")) status = "dnd";
            leagueClient.getXMPPClient().setCustomPresence(status, leagueClient.getCachedValue(CacheType.CHAT_STATUS), builder.build());
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) throws Exception {
        RiotMessageMessagePayload riotMessagePayload = riotMessageServiceMessage.getPayload();
        JSONObject payload = riotMessagePayload.getPayload();
        Logger.error(payload);
        switch (riotMessagePayload.getService()) {
            case "Parties" -> {
                Logger.error(payload);
                if (!payload.has("player") || payload.isNull("player")) return;
                PartiesRegistration registration = new PartiesRegistration(payload.getJSONObject("player"));
                CurrentParty currentParty = registration.getCurrentParty();
                if (currentParty == null) return;
                PartyGameMode gameMode = currentParty.getPartyGameMode();
                if (gameMode == null) {
                    setIdlePresence();
                } else if (!currentParty.isActivityLocked()) {
                    setLobbyPresence(payload, "chat");
                } else {
                    setQueuePresence();
                }
            }
            case "gsm" -> {
                if (riotMessagePayload.getResource().endsWith("player-credentials-update")) {
                    setInGamePresence(riotMessagePayload.getPayload());
                } else if (riotMessagePayload.getResource().endsWith("lol-gsm-server/v1/gsm/game-update")) {
                    if (!payload.has("gameState")) return;
                    if (!"TERMINATED".equals(payload.getString("gameState"))) return;
                    setIdlePresence();
                }
            }
            case "teambuilder" -> {
                if (!payload.has("phaseName")) return;
                if (payload.getString("phaseName").equals("MATCHMAKING")) {
                    setQueuePresence();
                }
            }
        }
    }

    private void setQueuePresence() throws Exception {
        Logger.error("INQUEUE");
        PartiesLedge partiesLedge = leagueClientUI.getLeagueClient().getLedge().getParties();
        PartiesRegistration registration = partiesLedge.getCurrentRegistration();
        Logger.error(registration);
        Presence.Builder builder = leagueClientUI.getLeagueClient().getCachedValue(CacheType.PRESENCE_BUILDER);
        int queueId = registration.getCurrentParty().getPartyGameMode().getQueueId();
        builder.setTimestamp(String.valueOf(System.currentTimeMillis()));
        builder.setGameMode(MapQueueId.getGameMode(queueId));
        builder.setGameQueueType(MapQueueId.getGameQueueType(queueId));
        builder.setGameStatus("inQueue");
        builder.setQueueId(String.valueOf(queueId));
        String status = leagueClientUI.getHeader().getChatStatus();
        set("default".equals(status) ? "dnd" : status, builder.build());
    }

    private void setLobbyPresence(JSONObject payload, String type) throws Exception {
        Logger.error("INLOBBY");
        Presence.Builder builder = leagueClient.getCachedValue(CacheType.PRESENCE_BUILDER);
        PartiesRegistration registration = new PartiesRegistration(payload.getJSONObject("player"));
        CurrentParty party = registration.getCurrentParty();
        PartyGameMode mode = party.getPartyGameMode();
        if (mode == null) return;
        builder.setGameMode(MapQueueId.getGameMode(mode.getQueueId()));
        builder.setGameQueueType(MapQueueId.getGameQueueType(mode.getQueueId()));
        String puuid = leagueClient.getCachedValue(CacheType.PUUID);
        party.getPlayers().stream()
                .filter(player -> player.getPUUID().equals(puuid))
                .findFirst()
                .ifPresent(self -> {
                    if (self.getRole().equals("LEADER")) {
                        builder.setGameStatus("hosting_" + MapQueueId.getGameQueueType(mode.getQueueId()));
                    } else {
                        builder.setGameStatus("outOfGame");
                    }
                    if (party.getPlayers().size() + 1 < party.getMaxPartySize()) {
                        JSONObject pty = new JSONObject();
                        pty.put("maxPlayers", party.getPartyGameMode().getMaxPartySize());
                        pty.put("partyId", party.getPartyId());
                        pty.put("queueId", party.getPartyGameMode().getQueueId());
                        JSONArray summoners = new JSONArray();
                        for (int i = 0; i < party.getPlayers().size(); i++) {
                            if (party.getPlayers().get(i).getRole().equals("MEMBER") || party.getPlayers().get(i).getRole().equals("LEADER")) {
                                summoners.put(party.getPlayers().get(i).getSummonerId());
                            }
                        }
                        pty.put("summoners", summoners);
                        builder.setPTY(pty.toString());
                    }
                    builder.setQueueId(String.valueOf(mode.getQueueId()));
                    String status = leagueClientUI.getHeader().getChatStatus();
                    set("default".equals(status) ? type : status, builder.build());
                });
    }

    private void setInGamePresence(JSONObject payload) throws Exception {
        Logger.error("INGAME");
        Presence.Builder builder = leagueClientUI.getLeagueClient().getCachedValue(CacheType.PRESENCE_BUILDER);
        String gameId = String.valueOf(payload.getLong("gameId"));
        String gameMode = payload.getString("gameMode");
        long summonerId = payload.getLong("summonerId");
        builder.setGameId(gameId);
        builder.setGameMode(gameMode);
        builder.setTimestamp(String.valueOf(System.currentTimeMillis()));
        GameServiceMessageLedge gmsLedge = leagueClientUI.getLeagueClient().getLedge().getGameServiceMessage();
        JSONObject data = gmsLedge.getGameInfoByGameId(gameId);
        builder.setIsObservable("ALL");
        builder.setGameStatus("inGame");
        JSONObject finalSummoner = getSelf(summonerId, data);
        data.getJSONArray("playerChampionSelections")
                .toList()
                .stream()
                .map(o -> (HashMap<?, ?>) o)
                .map(JSONObject::new)
                .filter(object -> object.has("summonerInternalName"))
                .filter(object -> object.getString("summonerInternalName").equals(finalSummoner.getString("summonerInternalName")))
                .findFirst()
                .ifPresent(choice -> {
                    builder.setChampionId(String.valueOf(choice.getInt("championId")));
                    builder.setGameQueueType(data.getString("queueTypeName"));
                    builder.setQueueId(String.valueOf(data.getInt("gameQueueConfigId")));
                    builder.setMapId(String.valueOf(data.getInt("mapId")));
                    String status = leagueClientUI.getHeader().getChatStatus();
                    set("default".equals(status) ? "dnd" : status, builder.build());
                });
    }

    private JSONObject getSelf(long summonerId, JSONObject data) {
        JSONObject summoner = new JSONObject();
        JSONArray teamOne = data.getJSONArray("teamOne");
        JSONArray teamTwo = data.getJSONArray("teamTwo");
        boolean found = false;
        for (int i = 0; i < teamOne.length(); i++) {
            summoner = teamOne.getJSONObject(i);
            if (!summoner.has("summonerId")) continue;
            if (summoner.getLong("summonerId") == summonerId) {
                found = true;
                break;
            }
        }
        if (!found) {
            for (int i = 0; i < teamTwo.length(); i++) {
                summoner = teamTwo.getJSONObject(i);
                if (!summoner.has("summonerId")) continue;
                if (summoner.getLong("summonerId") == summonerId) {
                    break;
                }
            }
        }
        return summoner;
    }
}
