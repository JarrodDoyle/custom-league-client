package com.hawolt.ui.queue;

import com.hawolt.Swiftrift;
import com.hawolt.async.Debouncer;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.data.PositionPreference;
import com.hawolt.client.resources.ledge.preferences.PlayerPreferencesLedge;
import com.hawolt.client.resources.ledge.preferences.objects.PreferenceType;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LComboBox;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.settings.SettingType;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created: 11/08/2023 23:00
 * Author: Twitter @hawolt
 **/

public class DraftGameLobby extends GameLobby implements ActionListener {
    private final Debouncer debouncer = new Debouncer();
    private LComboBox<PositionPreference> main, other;

    public DraftGameLobby(Swiftrift swiftrift, Container parent, CardLayout layout, QueueWindow queueWindow) {
        super(swiftrift, parent, layout, queueWindow);
    }

    @Override
    protected void createSpecificComponents(ChildUIComponent component) {
        ChildUIComponent roles = new ChildUIComponent(new GridLayout(0, 2, 5, 0));
        roles.setBorder(new EmptyBorder(5, 5, 5, 5));
        main = new LComboBox<>(PositionPreference.values());
        main.addActionListener(this);
        roles.add(main);
        other = new LComboBox<>(PositionPreference.values());
        other.addActionListener(this);
        roles.add(other);
        component.add(roles, BorderLayout.SOUTH);
        this.selectPositionPreference();
    }

    @Override
    protected void createGrid(ChildUIComponent component) {
        grid = new ChildUIComponent(new GridLayout(1, 5));
        for (int i = 0; i < 5; i++) grid.add(new DraftSummonerComponent());
        grid.setBackground(Color.YELLOW);
        component.add(grid, BorderLayout.CENTER);
    }


    @Override
    public DraftSummonerComponent getSummonerComponentAt(int id) {
        int index;
        if (id == 0) index = 2;
        else if (id == 1) index = 1;
        else if (id == 2) index = 3;
        else if (id == 3) index = 0;
        else index = 4;
        return (DraftSummonerComponent) grid.getComponent(index);
    }

    public void selectPositionPreference() {
        Swiftrift.service.execute(() -> {
            JSONObject data = swiftrift.getSettingService().getUserSettings().getPartyPositionPreference();
            main.setSelectedItem(PositionPreference.valueOf(data.getString("firstPreference")));
            other.setSelectedItem(PositionPreference.valueOf(data.getString("secondPreference")));
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PositionPreference primary = main.getItemAt(main.getSelectedIndex());
        PositionPreference secondary = other.getItemAt(other.getSelectedIndex());
        try {
            swiftrift.getLeagueClient().getLedge().getParties().metadata(primary, secondary);
        } catch (IOException ex) {
            Logger.error(ex);
        }
        debouncer.debounce("save", () -> {
            try {
                savePositionPreference();
            } catch (IOException ex) {
                Logger.error(e);
            }
        }, 5, TimeUnit.SECONDS);
    }

    public void savePositionPreference() throws IOException {
        PlayerPreferencesLedge playerPreferencesLedge = swiftrift.getLeagueClient().getLedge().getPlayerPreferences();
        JSONObject preference = swiftrift.getSettingService().getUserSettings().setPartyPositionPreference(
                new JSONObject()
                        .put("firstPreference", main.getItemAt(main.getSelectedIndex()).toString())
                        .put("secondPreference", other.getItemAt(other.getSelectedIndex()).toString())
        );
        playerPreferencesLedge.setPreferences(PreferenceType.LCU_PREFERENCES, preference.toString());
        swiftrift.getSettingService().write(SettingType.PLAYER, "preferences", preference);
    }
}
