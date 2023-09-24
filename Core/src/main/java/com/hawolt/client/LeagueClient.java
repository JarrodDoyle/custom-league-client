package com.hawolt.client;

import com.hawolt.client.cache.*;
import com.hawolt.client.handler.RMSHandler;
import com.hawolt.client.handler.RTMPHandler;
import com.hawolt.client.handler.XMPPHandler;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.preferences.objects.PreferenceType;
import com.hawolt.client.resources.platform.PlatformEndpoint;
import com.hawolt.client.resources.purchasewidget.PurchaseWidget;
import com.hawolt.generic.data.Platform;
import com.hawolt.generic.data.Unsafe;
import com.hawolt.logger.Logger;
import com.hawolt.rms.VirtualRiotMessageClient;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.virtual.leagueclient.client.VirtualLeagueClient;
import com.hawolt.virtual.leagueclient.instance.IVirtualLeagueClientInstance;
import com.hawolt.virtual.riotclient.client.IVirtualRiotClient;
import com.hawolt.virtual.riotclient.instance.IVirtualRiotClientInstance;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created: 04/06/2023 23:00
 * Author: Twitter @hawolt
 **/

public class LeagueClient implements Cacheable, Consumer<CachedValueLoader<?>> {

    private final Map<CacheType, List<CacheListener<?>>> map = new HashMap<>();
    private final Map<CacheType, Object> cache = new HashMap<>();

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
        ExecutorService service = Executors.newCachedThreadPool();
        // TODO DONT STORE TOKEN LIKE THIS FIX
        service.execute(new CachedValueLoader<>(CacheType.INVENTORY_TOKEN, () -> ledge.getInventoryService().getInventoryToken(), this));
        service.execute(new CachedValueLoader<>(CacheType.CHAT_STATUS, () -> getLedge().getPlayerPreferences().getPreferences(PreferenceType.LCU_SOCIAL_PREFERENCES).getString("chat-status-message"), this));
        service.execute(new CachedValueLoader<>(CacheType.SUMMONER_ID, () -> getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId(), this));
        service.execute(new CachedValueLoader<>(CacheType.ACCOUNT_ID, () -> getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeague().getCUID(), this));
        // TODO DONT STORE TOKEN LIKE THIS FIX
        service.execute(new CachedValueLoader<>(CacheType.CHAMPION_DATA, () -> ledge.getInventoryService().getInventoryToken(), this));
        service.execute(new CachedValueLoader<>(CacheType.PUUID, () -> getVirtualRiotClient().getRiotClientUser().getPUUID(), this));
        service.execute(new CachedValueLoader<>(CacheType.PROFILE, () -> ledge.getSummoner().resolveSummonerById(getVirtualLeagueClientInstance().getUserInformation().getUserInformationLeagueAccount().getSummonerId()), this));
        service.execute(new CachedValueLoader<>(CacheType.RANKED_STATISTIC, () -> ledge.getLeague().getRankedStats(getVirtualRiotClient().getRiotClientUser().getPUUID()), this));
        service.execute(new CachedValueLoader<>(CacheType.PARTY_REGISTRATION, () -> ledge.getParties().register(), this));
        service.shutdown();
    }

    @Override
    public void accept(CachedValueLoader<?> loader) {
        if (loader.getException() != null) {
            Logger.error("Failed to cache value for {}", loader.getType());
        } else {
            Logger.info("Cache value for {}", loader.getType());
            cache(loader.getType(), loader.getValue());
        }
    }

    @Override
    public void cache(CacheType type, Object o) {
        this.cache.put(type, o);
        this.dispatch(type);
    }

    public void dispatch(CacheType type) {
        if (!map.containsKey(type)) return;
        Object reference = cache.get(type);
        this.map.get(type).forEach(listener -> listener.onCacheUpdate(Unsafe.cast(reference)));
    }

    @Override
    public void register(CacheType type, CacheListener<?> listener) {
        if (!map.containsKey(type)) map.put(type, new LinkedList<>());
        this.map.get(type).add(listener);
        this.dispatch(type);
    }

    public <T> T getCachedValue(CacheType type) {
        Logger.info("Get cache value for {}", type);
        return Unsafe.cast(cache.get(type));
    }

    public <T> T getCachedValueOrElse(CacheType type, ExceptionalSupplier<T> supplier) throws Exception {
        if (cache.containsKey(type)) {
            return getCachedValue(type);
        } else {
            T reference = supplier.get();
            cache.put(type, reference);
            return reference;
        }
    }

    public <T> Optional<T> getCachedValueOrElse(CacheType type, ExceptionalSupplier<T> supplier, Consumer<Exception> consumer) {
        try {
            return Optional.of(getCachedValueOrElse(type, supplier));
        } catch (Exception e) {
            consumer.accept(e);
            return Optional.empty();
        }
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
}
