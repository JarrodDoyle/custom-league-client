package com.hawolt.ui.queue;

import com.hawolt.client.resources.ledge.parties.objects.PartyMember;
import com.hawolt.client.resources.ledge.parties.objects.PartyParticipantMetadata;

import java.awt.*;

/**
 * Created: 21/08/2023 18:34
 * Author: Twitter @hawolt
 **/

public class DraftSummonerComponent extends SummonerComponent {
    private static final Font ROLE_FONT = new Font("Arial", Font.BOLD, 16);

    public DraftSummonerComponent() {
        super();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dimensions = getSize();
        Graphics2D g2d = (Graphics2D) g;

        // We don't want to draw anything if there's nobody actually here
        if (summoner == null || participant == null) return;
        String role = participant.getRole();
        if (!role.equals("MEMBER") && !role.equals("LEADER")) return;
        PartyMember member = (PartyMember) participant;
        if (member == null) return;
        PartyParticipantMetadata metadata = member.getParticipantMetadata();
        if (metadata == null) return;

        int centeredX = dimensions.width >> 1;
        int centeredY = dimensions.height >> 1;
        int positionY = centeredY + (IMAGE_DIMENSION.height >> 1) + 40;
        g2d.setFont(ROLE_FONT);
        paintRoleSelection(g2d, centeredX, positionY, metadata.getPrimaryPreference(), 0);
        paintRoleSelection(g2d, centeredX, positionY, metadata.getSecondaryPreference(), 1);
    }

    private void paintRoleSelection(Graphics2D g2d, int centeredX, int positionY, String role, int index) {
        FontMetrics metrics = g2d.getFontMetrics();

        g2d.setColor(Color.DARK_GRAY);
        int additionalSpacing = 5, gapSpacing = 6;
        int x = gapSpacing + (index * centeredX);
        int primaryX = (centeredX - (index == 0 ? (centeredX >> 1) : (-1 * (centeredX >> 1)))) - (metrics.stringWidth(role) >> 1);

        g2d.fillRoundRect(x, positionY - metrics.getAscent() - additionalSpacing, centeredX - (gapSpacing << 1), 18 + (additionalSpacing << 1), 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawString(role, primaryX, positionY);
    }

}
