package com.hawolt.ui.queue;

import com.hawolt.Swiftrift;
import com.hawolt.async.ExecutorManager;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.*;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyAction;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyRole;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.impl.payload.RiotMessageMessagePayload;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created: 11/08/2023 23:00
 * Author: Twitter @hawolt
 **/

public abstract class GameLobby extends ChildUIComponent implements IServiceMessageListener<RiotMessageServiceMessage> {
    public final ScheduledExecutorService scheduler = ExecutorManager.getScheduledService("queue-resumer");

    public final Swiftrift swiftrift;
    public ScheduledFuture<?> future;
    public int queueId;

    public ChildUIComponent component = new ChildUIComponent(new BorderLayout());
    public ChildUIComponent grid;
    private LFlatButton stop, start;
    private CurrentParty party;
    private String puuid;

    public GameLobby(Swiftrift swiftrift, Container parent, CardLayout layout, QueueWindow queueWindow) {
        super(new BorderLayout());

        createGrid(component);
        ChildUIComponent newButton = new ChildUIComponent(new BorderLayout());
        this.swiftrift = swiftrift;
        this.swiftrift.getLeagueClient().getRMSClient().getHandler().addMessageServiceListener(MessageService.PARTIES, this);
        this.swiftrift.getLeagueClient().getRMSClient().getHandler().addMessageServiceListener(MessageService.LOL_PLATFORM, this);

        ChildUIComponent top = new ChildUIComponent(new GridLayout(0, 1, 0, 0));
        LFlatButton close = new LFlatButton("Choose mode", LTextAlign.CENTER, HighlightType.COMPONENT);
        close.addActionListener(listener -> layout.show(parent, "modes"));

        LFlatButton invite = new LFlatButton("Invite another Summoner", LTextAlign.CENTER, HighlightType.COMPONENT);
        LFlatButton leave = new LFlatButton("Leave Party", LTextAlign.CENTER, HighlightType.COMPONENT);
        leave.addActionListener(listener -> {
            this.stop.setEnabled(false);
            this.start.setEnabled(true);
            layout.show(parent, "modes");
            try {
                swiftrift.getLeagueClient().getLedge().getParties().role(PartyRole.DECLINED);
                queueId = 0;
                queueWindow.rebase();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        newButton.add(leave, BorderLayout.NORTH);
        close.addActionListener(listener -> {
            layout.show(parent, "modes");
        });
        newButton.add(close, BorderLayout.CENTER);
        add(newButton, BorderLayout.NORTH);
        invite.addActionListener(listener -> {
            String name = Swiftrift.showInputDialog("Who do you want to Invite?");
            if (name == null) return;
            LedgeEndpoint ledges = swiftrift.getLeagueClient().getLedge();
            SummonerLedge summonerLedge = ledges.getSummoner();
            PartiesLedge partiesLedge = ledges.getParties();
            try {
                Summoner summoner = summonerLedge.resolveSummonerByName(name);
                partiesLedge.invite(summoner.getPUUID());
            } catch (IOException | PartyException e) {
                Logger.error(e);
            }
        });
        top.add(close);
        top.add(invite);
        component.add(top, BorderLayout.NORTH);
        Swiftrift.service.execute(() -> createSpecificComponents(component));

        add(component, BorderLayout.CENTER);
        ChildUIComponent bottom = new ChildUIComponent(new GridLayout(0, 2, 5, 0));
        bottom.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.start = new LFlatButton("Start", LTextAlign.CENTER, HighlightType.COMPONENT);
        start.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
        start.setBackground(ColorPalette.buttonSelectionColor);
        start.setHighlightColor(ColorPalette.buttonSelectionAltColor);
        start.addActionListener(listener -> startQueue());
        this.stop = new LFlatButton("×", LTextAlign.CENTER, HighlightType.COMPONENT);
        stop.setEnabled(false);
        stop.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
        stop.setBackground(ColorPalette.buttonSelectionColor);
        stop.setHighlightColor(ColorPalette.buttonSelectionAltColor);
        stop.addActionListener(listener -> {
            if (future != null) future.cancel(true);
            PartiesLedge partiesLedge = swiftrift.getLeagueClient().getLedge().getParties();
            PartiesRegistration registration = partiesLedge.getCurrentRegistration();
            try {
                if (registration == null) return;
                partiesLedge.setQueueAction(PartyAction.STOP);
                swiftrift.getChatSidebar().getEssentials().disableQueueState();
                this.flipButtonState();
            } catch (IOException e) {
                Logger.error(e);
            }
        });
        bottom.add(stop);
        bottom.add(start);
        add(bottom, BorderLayout.SOUTH);
    }

    public LFlatButton getStopButton() {
        return stop;
    }

    public LFlatButton getStartButton() {
        return start;
    }

    public void toggleButtonState(boolean stop, boolean start) {
        this.start.setEnabled(start);
        this.stop.setEnabled(stop);
    }

    public void flipButtonState() {
        this.toggleButtonState(!stop.isEnabled(), !start.isEnabled());
    }

    protected abstract void createSpecificComponents(ChildUIComponent component);

    protected abstract void createGrid(ChildUIComponent component);

    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) {
        RiotMessageMessagePayload base = riotMessageServiceMessage.getPayload();
        JSONObject payload = base.getPayload();
        if (base.getResource().endsWith("teambuilder/v1/removedFromServiceV1")) {
            this.flipButtonState();
        } else if (payload.has("player") && !payload.isNull("player")) {
            PartiesRegistration registration = new PartiesRegistration(payload.getJSONObject("player"));
            puuid = registration.getPUUID();
            party = registration.getCurrentParty();
            List<PartyParticipant> partyParticipants = party.getPlayers();
            if (party == null) return;
            PartyRestriction restriction = party.getPartyRestriction();
            if (restriction != null) handleGatekeeperRestriction(restriction.getRestrictionList());
            partyParticipants.stream().filter(participant -> participant.getPUUID().equals(puuid)).findFirst().ifPresent(self -> {
                SummonerLedge summonerLedge = swiftrift.getLeagueClient().getLedge().getSummoner();
                try {
                    getSummonerComponentAt(0).update(self, summonerLedge.resolveSummonerByPUUD(puuid));
                    partyParticipants.remove(self);
                    int memberPosition = 1;
                    for (PartyParticipant participant : partyParticipants) {
                        Summoner summoner = summonerLedge.resolveSummonerByPUUD(participant.getPUUID());
                        if (participant.getRole().equals("MEMBER") || participant.getRole().equals("LEADER")) {
                            getSummonerComponentAt(memberPosition++).update(participant, summoner);
                        }
                    }
                    for (int i = memberPosition; i < party.getMaxPartySize(); i++) {
                        getSummonerComponentAt(i).update(null, null);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                revalidate();
            });
        }
    }

    private void handleGatekeeperRestriction(List<GatekeeperRestriction> restrictions) {
        List<GatekeeperRestriction> sorted = restrictions.stream()
                .sorted(((o1, o2) -> Long.compare(o2.getRemainingMillis(), o1.getRemainingMillis())))
                .toList();
        GatekeeperRestriction gatekeeperRestriction = sorted.get(0);
        Logger.debug("Restriction: {}", gatekeeperRestriction);
        swiftrift.getChatSidebar().getEssentials().toggleQueueState(
                System.currentTimeMillis(),
                gatekeeperRestriction.getRemainingMillis(),
                true
        );
        future = scheduler.schedule(() -> {
            try {
                swiftrift.getLeagueClient().getLedge().getParties().resume();
            } catch (IOException e) {
                Logger.error(e);
            }
        }, gatekeeperRestriction.getRemainingMillis(), TimeUnit.MILLISECONDS);
    }

    abstract public SummonerComponent getSummonerComponentAt(int id);

    public void startQueue() {
        PartiesLedge partiesLedge = swiftrift.getLeagueClient().getLedge().getParties();
        try {
            partiesLedge.ready();
            JSONObject response = partiesLedge.setQueueAction(PartyAction.START);
            List<GatekeeperRestriction> direct = response.has("errorCode") &&
                    "GATEKEEPER_RESTRICTED".equals(response.getString("errorCode")) ?
                    new PartyGatekeeper(response).getRestrictionList() : new ArrayList<>();
            CurrentParty party = partiesLedge.getOwnPlayer().getCurrentParty();
            PartyRestriction restriction = party.getPartyRestriction();
            List<GatekeeperRestriction> indirect = restriction != null ?
                    restriction.getRestrictionList() : new ArrayList<>();
            this.flipButtonState();
            if (direct.isEmpty() && indirect.isEmpty()) return;
            handleGatekeeperRestriction(
                    Stream.of(direct, indirect).flatMap(Collection::stream).collect(Collectors.toList())
            );
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}
