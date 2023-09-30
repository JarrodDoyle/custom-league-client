package com.hawolt.ui.champselect.impl.aram;

import com.hawolt.Swiftrift;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.service.impl.TeamBuilderService;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.champselect.generic.impl.ChampSelectBenchElement;
import com.hawolt.ui.champselect.generic.impl.ChampSelectChoice;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import org.json.JSONArray;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Created: 10/09/2023 17:32
 * Author: Twitter @hawolt
 **/

public class ARAMBenchUI extends ChampSelectUIComponent {
    private final ChampSelectBenchElement[] elements = new ChampSelectBenchElement[10];

    public ARAMBenchUI(AbstractRenderInstance instance) {
        instance.register(this);
        ColorPalette.addThemeListener(this);
        this.setLayout(new BorderLayout());
        this.setBackground(ColorPalette.backgroundColor);
        this.setPreferredSize(new Dimension(0, 80));
        this.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLACK),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );
        ChildUIComponent grid = new ChildUIComponent(new GridLayout(0, 10, 5, 0));
        for (int i = 0; i < elements.length; i++) {
            ChampSelectBenchElement element = new ChampSelectBenchElement(instance);
            element.setChampionId(-1);
            elements[i] = element;
            grid.add(element);
        }
        add(grid, BorderLayout.CENTER);

        LFlatButton button = new LFlatButton("⟳", LTextAlign.CENTER, HighlightType.COMPONENT);
        button.addActionListener(listener -> Swiftrift.service.execute(() -> {
            ChampSelectContext context = instance.getContext();
            LeagueRtmpClient client = context.getChampSelectDataContext().getLeagueClient().getRTMPClient();
            TeamBuilderService teamBuilderService = client.getTeamBuilderService();
            try {
                teamBuilderService.rerollV1Asynchronous(new PacketCallback() {
                    @Override
                    public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) throws Exception {
                        Logger.info(typedObject);
                    }
                });
            } catch (IOException e) {
                Logger.error("Unable to reroll");
                Logger.error(e);
            }
        }));
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 50));
        add(button, BorderLayout.EAST);
    }

    @Override
    public void init(ChampSelectContext context) {
        for (ChampSelectBenchElement element : elements) {
            element.setChampionId(-1);
        }
    }

    @Override
    public void update(ChampSelectContext context) {
        JSONArray bench = context.getChampSelectSettingsContext().getChampionBench();
        for (int i = 0; i < bench.length(); i++) {
            elements[i].setChampionId(bench.getInt(i));
        }
    }
}
