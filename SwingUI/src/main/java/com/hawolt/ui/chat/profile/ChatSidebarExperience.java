package com.hawolt.ui.chat.profile;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;

import java.awt.*;

public class ChatSidebarExperience extends ChildUIComponent {
    private final Color UNOBTAINED = new Color(231, 97, 97);
    private final Color GAINED = new Color(93, 156, 89);
    private int current, total;

    public ChatSidebarExperience(UserInformation information, LayoutManager layout) {
        this.setBackground(ColorPalette.accentColor);
        this.setPreferredSize(new Dimension(320, 8));
        if (!information.isLeagueAccountAssociated()) return;

        // Placeholder XP amounts
        this.current = 419;
        this.total = 2193;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension dimension = getSize();
        double progress = ((double) current / (double) total);
        int gainedWidth = (int) Math.floor(progress * dimension.width);

        g.setColor(GAINED);
        g.fillRect(0, 0, gainedWidth, dimension.height);
        g.setColor(UNOBTAINED);
        g.fillRect(gainedWidth, 0, dimension.width - gainedWidth, dimension.height);
    }
}
