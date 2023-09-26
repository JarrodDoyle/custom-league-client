package com.hawolt.client;

import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.cache.CachedValueLoader;
import com.hawolt.client.cache.ClientCache;
import com.hawolt.client.exceptional.ExceptionalFunction;
import com.hawolt.client.exceptional.impl.ArrogantConsumer;
import com.hawolt.client.handler.RMSHandler;
import com.hawolt.client.handler.RTMPHandler;
import com.hawolt.client.handler.XMPPHandler;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.preferences.objects.PreferenceType;
import com.hawolt.client.resources.platform.PlatformEndpoint;
import com.hawolt.client.resources.purchasewidget.PurchaseWidget;
import com.hawolt.generic.data.Platform;
import com.hawolt.io.RunLevel;
import com.hawolt.rms.VirtualRiotMessageClient;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.virtual.leagueclient.client.VirtualLeagueClient;
import com.hawolt.virtual.leagueclient.instance.IVirtualLeagueClientInstance;
import com.hawolt.virtual.riotclient.client.IVirtualRiotClient;
import com.hawolt.virtual.riotclient.instance.IVirtualRiotClientInstance;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;

import javax.imageio.ImageIO;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Created: 04/06/2023 23:00
 * Author: Twitter @hawolt
 **/

public class LeagueClient extends ClientCache implements PacketCallback {
    private final IVirtualLeagueClientInstance virtualLeagueClientInstance;
    private final VirtualLeagueClient virtualLeagueClient;

    private final IVirtualRiotClientInstance virtualRiotClientInstance;
    private final IVirtualRiotClient virtualRiotClient;

    private PurchaseWidget purchaseWidget;
    private PlatformEndpoint platform;
    private LedgeEndpoint ledge;

    private XMPPHandler xmpp;
    private RTMPHandler rtmp;
    private RMSHandler rms;

    public LeagueClient(VirtualLeagueClient virtualLeagueClient) {
        this.virtualLeagueClientInstance = virtualLeagueClient.getVirtualLeagueClientInstance();
        this.virtualRiotClient = virtualLeagueClientInstance.getVirtualRiotClient();
        this.virtualRiotClientInstance = virtualRiotClient.getInstance();
        this.virtualLeagueClient = virtualLeagueClient;
        this.configure();
    }

    private void configure() {
        this.purchaseWidget = new PurchaseWidget(this);
        this.platform = new PlatformEndpoint(this);
        this.ledge = new LedgeEndpoint(this);
        this.cache();
    }

    private void cache() {
        cache(CacheElement.PUUID, getVirtualRiotClient().getRiotClientUser().getPUUID());
        cache(CacheElement.ACCOUNT_ID, getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeague().getCUID());
        cache(CacheElement.SUMMONER_ID, getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId());
        setElementSource(CacheElement.INVENTORY_TOKEN, (ExceptionalFunction<LeagueClient, String>) client -> ledge.getInventoryService().getInventoryToken());
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(new CachedValueLoader<>(CacheElement.PROFILE_MASK, () -> ImageIO.read(RunLevel.get("assets/profile-mask.png")), this));
        service.execute(new CachedValueLoader<>(CacheElement.CHAT_STATUS, () -> getLedge().getPlayerPreferences().getPreferences(PreferenceType.LCU_SOCIAL_PREFERENCES).getString("chat-status-message"), () -> "", this));
        service.execute(new CachedValueLoader<>(CacheElement.SUMMONER, () -> ledge.getSummoner().resolveSummonerById(getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId()), this));
        service.execute(new CachedValueLoader<>(CacheElement.PROFILE, () -> ledge.getSummoner().resolveSummonerProfile(getVirtualRiotClient().getRiotClientUser().getPUUID()), this));
        service.execute(new CachedValueLoader<>(CacheElement.RANKED_STATISTIC, () -> ledge.getLeague().getRankedStats(getVirtualRiotClient().getRiotClientUser().getPUUID()), this));
        service.shutdown();
    }

    public IVirtualLeagueClientInstance getVirtualLeagueClientInstance() {
        return virtualLeagueClientInstance;
    }

    public VirtualLeagueClient getVirtualLeagueClient() {
        return virtualLeagueClient;
    }

    public IVirtualRiotClientInstance getVirtualRiotClientInstance() {
        return virtualRiotClientInstance;
    }

    public IVirtualRiotClient getVirtualRiotClient() {
        return virtualRiotClient;
    }

    public PurchaseWidget getPurchaseWidget() {
        return purchaseWidget;
    }

    public PlatformEndpoint getPlatform() {
        return platform;
    }

    public LedgeEndpoint getLedge() {
        return ledge;
    }

    public RMSHandler getRMS() {
        return rms;
    }

    public void setRMS(RMSHandler rms) {
        this.rms = rms;
    }

    public VirtualRiotMessageClient getRMSClient() {
        return rms.getVirtualRiotMessageClient();
    }

    public XMPPHandler getXMPP() {
        return xmpp;
    }

    public void setXMPP(XMPPHandler xmpp) {
        this.xmpp = xmpp;
    }

    public VirtualRiotXMPPClient getXMPPClient() {
        return xmpp.getVirtualRiotXMPPClient();
    }

    public RTMPHandler getRTMP() {
        return rtmp;
    }

    public void setRTMP(RTMPHandler rtmp) {
        this.rtmp = rtmp;
    }

    public LeagueRtmpClient getRTMPClient() {
        return rtmp.getVirtualLeagueRTMPClient();
    }

    public Platform getPlayerPlatform() {
        return virtualLeagueClientInstance.getPlatform();
    }

    @Override
    protected LeagueClient getClient() {
        return this;
    }

    public void wrap() {
        this.rtmp.getVirtualLeagueRTMPClient().addDefaultCallback(this);
        ArrogantConsumer.consume(client -> client.getClientFacadeService().getLoginDataPacketForUserAsynchronous(LeagueClient.this), getRTMPClient());
    }

    @Override
    public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) throws Exception {
        if (!typedObject.containsKey("data")) return;
        TypedObject data = typedObject.getTypedObject("data");
        if (!data.containsKey("flex.messaging.messages.AcknowledgeMessage")) return;
        TypedObject message = data.getTypedObject("flex.messaging.messages.AcknowledgeMessage");
        if (!message.containsKey("body")) return;
        Object object = message.get("body");
        if (!(object instanceof TypedObject body)) return;
        if (!body.containsKey("com.riotgames.platform.clientfacade.domain.LoginDataPacket")) return;
        TypedObject packet = body.getTypedObject("com.riotgames.platform.clientfacade.domain.LoginDataPacket");
        cache(CacheElement.LOGIN_DATA_PACKET, packet);
        if (!packet.containsKey("clientSystemStates")) return;
        TypedObject states = packet.getTypedObject("clientSystemStates");
        if (!states.containsKey("com.riotgames.platform.systemstate.ClientSystemStatesNotification")) return;
        TypedObject notification = states.getTypedObject("com.riotgames.platform.systemstate.ClientSystemStatesNotification");
        TypedObject freeToPlayChampionForNewPlayersIdList = notification.getTypedObject("freeToPlayChampionForNewPlayersIdList");
        cache(CacheElement.F2P_NEW_PLAYER, convertToIntArray(convertToList(freeToPlayChampionForNewPlayersIdList, o -> ((Double) o).intValue())));
        TypedObject freeToPlayChampionIdList = notification.getTypedObject("freeToPlayChampionIdList");
        cache(CacheElement.F2P_VETERAN_PLAYER, convertToIntArray(convertToList(freeToPlayChampionIdList, o -> ((Double) o).intValue())));
        cache(CacheElement.FREE_TO_PLAY_LEVEL_CAP, notification.getInteger("freeToPlayChampionsForNewPlayersMaxLevel"));
    }

    private <T> List<T> convertToList(TypedObject reference, Function<Object, T> function) {
        Object[] array = (Object[]) reference.getTypedObject("flex.messaging.io.ArrayCollection").get("source");
        List<T> list = new LinkedList<>();
        for (Object o : array) {
            list.add(function.apply(o));
        }
        return list;
    }

    private int[] convertToIntArray(List<Integer> list) {
        return list.stream().mapToInt(Integer::intValue).toArray();
    }
}
