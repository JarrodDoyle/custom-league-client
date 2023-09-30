package com.hawolt.ui.champselect.context.impl;

import com.hawolt.client.LeagueClient;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.utility.Base64GZIP;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.ui.champselect.ChampSelectUI;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectContextProvider;
import com.hawolt.ui.champselect.context.ChampSelectDataContext;
import com.hawolt.xmpp.event.handler.message.IMessageListener;
import com.hawolt.xmpp.event.objects.conversation.history.impl.FailedMessage;
import com.hawolt.xmpp.event.objects.conversation.history.impl.IncomingMessage;
import com.hawolt.xmpp.event.objects.conversation.history.impl.OutgoingMessage;
import com.hawolt.xmpp.event.objects.presence.impl.JoinMucPresence;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 10/09/2023 03:27
 * Author: Twitter @hawolt
 **/

public class ChampSelectData extends ChampSelectContextProvider implements ChampSelectDataContext, PacketCallback, IMessageListener {

    private final Map<String, String> resolver = new HashMap<>();

    public ChampSelectData(ChampSelectUI champSelectUI, ChampSelectContext context) {
        super(champSelectUI, context);
    }

    @Override
    public IMessageListener getMessageListener() {
        return this;
    }

    @Override
    public PacketCallback getPacketCallback() {
        return this;
    }

    @Override
    public void cache(String puuid, String name) {
        Logger.info("[champ-select] resolve name in cache for {} as {}", puuid, name);
        this.resolver.put(puuid, name);
    }


    @Override
    public Map<String, String> getPUUIDResolver() {
        return resolver;
    }

    @Override
    public LeagueClient getLeagueClient() {
        return champSelectUI.getLeagueClient();
    }

    @Override
    public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) throws Exception {
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
            Logger.info("[champ-select] {}", payload);
            context.getChampSelectSettingsContext().populate(payload);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    @Override
    public void onMessageReceived(IncomingMessage incomingMessage) {
        if (!incomingMessage.getType().equals("groupchat")) return;
        this.champSelectUI.getInstance(context.getChampSelectSettingsContext().getQueueId()).push(incomingMessage);
    }

    @Override
    public void push(JoinMucPresence message) {
        this.champSelectUI.getInstance(context.getChampSelectSettingsContext().getQueueId()).push(message);
    }

    @Override
    public void onMessageSent(OutgoingMessage outgoingMessage) {

    }

    @Override
    public void onFailedMessage(FailedMessage failedMessage) {

    }
}
