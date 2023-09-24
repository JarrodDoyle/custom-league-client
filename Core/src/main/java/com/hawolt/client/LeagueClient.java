package com.hawolt.client;

import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.cache.CachedValueLoader;
import com.hawolt.client.cache.ClientCache;
import com.hawolt.client.cache.ExceptionalFunction;
import com.hawolt.client.handler.RMSHandler;
import com.hawolt.client.handler.RTMPHandler;
import com.hawolt.client.handler.XMPPHandler;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.preferences.objects.PreferenceType;
import com.hawolt.client.resources.platform.PlatformEndpoint;
import com.hawolt.client.resources.purchasewidget.PurchaseWidget;
import com.hawolt.generic.data.Platform;
import com.hawolt.rms.VirtualRiotMessageClient;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.virtual.leagueclient.client.VirtualLeagueClient;
import com.hawolt.virtual.leagueclient.instance.IVirtualLeagueClientInstance;
import com.hawolt.virtual.riotclient.client.IVirtualRiotClient;
import com.hawolt.virtual.riotclient.instance.IVirtualRiotClientInstance;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: 04/06/2023 23:00
 * Author: Twitter @hawolt
 **/

public class LeagueClient extends ClientCache {
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
        cache(CacheElement.SUMMONER_ID, () -> ledge.getParties().register());
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(new CachedValueLoader<>(CacheElement.CHAT_STATUS, () -> getLedge().getPlayerPreferences().getPreferences(PreferenceType.LCU_SOCIAL_PREFERENCES).getString("chat-status-message"), this));
        service.execute(new CachedValueLoader<>(CacheElement.PROFILE, () -> ledge.getSummoner().resolveSummonerById(getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId()), this));
        service.execute(new CachedValueLoader<>(CacheElement.RANKED_STATISTIC, () -> ledge.getLeague().getRankedStats(getVirtualRiotClient().getRiotClientUser().getPUUID()), this));
        service.shutdown();
        setElementSource(CacheElement.INVENTORY_TOKEN, (ExceptionalFunction<LeagueClient, String>) client -> ledge.getInventoryService().getInventoryToken());
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
}
