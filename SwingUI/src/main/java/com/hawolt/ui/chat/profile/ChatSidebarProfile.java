package com.hawolt.ui.chat.profile;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;

import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 08/08/2023 17:25
 * Author: Twitter @hawolt
 **/

public class ChatSidebarProfile extends ChildUIComponent {
    private final ChatSidebarExperience experience;
    private final ChatSidebarProfileIcon icon;
    private final ChatSideBarUIControl control;
    private final ChatSidebarName name;
    private final ChatSidebarStatus status;

    public ChatSidebarProfile(UserInformation information, LayoutManager layout) {
        super(layout);
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
        statusLevelContainer.add(new ChatSidebarLevel(information, new BorderLayout()), BorderLayout.EAST);

        ChildUIComponent center = new ChildUIComponent(new BorderLayout());
        center.setBackground(ColorPalette.accentColor);
        center.add(control = new ChatSideBarUIControl(), BorderLayout.NORTH);
        center.add(name = new ChatSidebarName(), BorderLayout.CENTER);
        center.add(statusLevelContainer, BorderLayout.SOUTH);

        this.add(iconContainer, BorderLayout.WEST);
        this.add(center, BorderLayout.CENTER);
        this.add(experience = new ChatSidebarExperience(information, new BorderLayout()), BorderLayout.SOUTH);
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
}
