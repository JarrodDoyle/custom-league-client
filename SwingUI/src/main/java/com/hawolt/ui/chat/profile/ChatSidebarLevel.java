package com.hawolt.ui.chat.profile;

import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.paint.PaintHelper;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;

import java.awt.*;

public class ChatSidebarLevel extends ChildUIComponent {
    private final Font font = new Font("Arial", Font.BOLD, 16);
    private int level = 0;

    public ChatSidebarLevel(UserInformation information, LayoutManager layout) {
        this.setBackground(ColorPalette.accentColor);
        if (information.isLeagueAccountAssociated()) {
            this.level = (int) information.getUserInformationLeagueAccount().getSummonerLevel();
        }
        this.setPreferredSize(new Dimension(16 + String.valueOf(this.level).length() * font.getSize(), 8 + font.getSize()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension dimensions = getSize();
        int y = dimensions.height - (font.getSize() + 8);
        g.setColor(ColorPalette.backgroundColor);
        PaintHelper.roundedSquare((Graphics2D) g, 0, y, dimensions.width, dimensions.height - y, ColorPalette.CARD_ROUNDING, false, true, false, false);
        PaintHelper.drawShadowText(g, font, String.valueOf(this.level), new Rectangle(0, y, dimensions.width, dimensions.height - y), LTextAlign.CENTER, Color.WHITE);
    }
}
