package com.hawolt.ui.chat.profile;

import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.rms.data.impl.payload.RiotMessageMessagePayload;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 08/08/2023 17:25
 * Author: Twitter @hawolt
 **/

public class ChatSidebarProfile extends ChildUIComponent implements IServiceMessageListener<RiotMessageServiceMessage> {
    private final ChatSidebarExperience experience;
    private final ChatSideBarUIControl control;
    private final ChatSidebarProfileIcon icon;
    private final ChatSidebarStatus status;
    private final ChatSidebarLevel level;
    private final ChatSidebarName name;

    public ChatSidebarProfile(UserInformation information, Summoner summoner) {
        super(new BorderLayout());
        this.setBackground(ColorPalette.accentColor);
        this.setPreferredSize(new Dimension(300, 90));

        //had to make a container 'cause to put the header buttons in the corner i had to change the border here, and then the icon wasn't in the proper
        //position, not even by setting a border on it, like this it looks like it did before
        ChildUIComponent iconContainer = new ChildUIComponent(new BorderLayout());
        iconContainer.setBackground(ColorPalette.accentColor);
        iconContainer.setBorder(new EmptyBorder(8, 8, 8, 8));
        iconContainer.add(icon = new ChatSidebarProfileIcon(information, new BorderLayout()), BorderLayout.CENTER);

        ChildUIComponent statusLevelContainer = new ChildUIComponent(new BorderLayout());
        statusLevelContainer.setBackground(ColorPalette.accentColor);
        statusLevelContainer.setPreferredSize(new Dimension(320, 32));
        statusLevelContainer.add(status = new ChatSidebarStatus(), BorderLayout.CENTER);
        statusLevelContainer.add(level = new ChatSidebarLevel(information), BorderLayout.EAST);

        ChildUIComponent center = new ChildUIComponent(new BorderLayout());
        center.setBackground(ColorPalette.accentColor);
        center.add(control = new ChatSideBarUIControl(), BorderLayout.NORTH);
        center.add(name = new ChatSidebarName(), BorderLayout.CENTER);
        center.add(statusLevelContainer, BorderLayout.SOUTH);

        this.add(iconContainer, BorderLayout.WEST);
        this.add(center, BorderLayout.CENTER);
        this.add(experience = new ChatSidebarExperience(information, summoner), BorderLayout.SOUTH);
    }

    public ChatSidebarExperience getExperience() {
        return experience;
    }

    public ChatSidebarName getChatSidebarName() {
        return name;
    }

    public ChatSidebarStatus getStatus() {
        return status;
    }

    public ChatSidebarProfileIcon getIcon() {
        return icon;
    }

    public ChatSideBarUIControl getUIControl() {
        return control;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //Drawing top left rounding
        int width = getWidth();
        int height = getHeight();
        g2d.setColor(ColorPalette.accentColor);
        g2d.fillRect(0, 0, width, height);

    }

    @Override
    public void onMessage(RiotMessageServiceMessage messageServiceMessage) throws Exception {
        RiotMessageMessagePayload base = messageServiceMessage.getPayload();
        if (!base.getResource().endsWith("summoner/v1/xp")) return;
        JSONObject payload = base.getPayload();
        if (!payload.has("level")) return;
        JSONObject level = payload.getJSONObject("level");
        this.level.setLevel(level.getInt("finalLevel"));
        if (!level.has("progress")) return;
        JSONObject progress = level.getJSONObject("progress");
        experience.set(progress.getInt("finalXp"), progress.getInt("finalLevelBoundary"));
    }
}
