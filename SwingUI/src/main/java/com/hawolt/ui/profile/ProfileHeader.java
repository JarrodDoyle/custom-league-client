package com.hawolt.ui.profile;

import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.translucent.TranslucentButtonGroup;
import com.hawolt.ui.generic.translucent.TranslucentRadioButton;
import com.hawolt.ui.generic.translucent.TranslucentTextField;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 24/09/2023 14:02
 * Author: Twitter @hawolt
 **/

public class ProfileHeader extends ChildUIComponent {
    private final TranslucentRadioButton overview, history;
    private final TranslucentTextField search;

    public ProfileHeader() {
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        this.overview = new TranslucentRadioButton("Overview", LTextAlign.LEFT, HighlightType.TEXT);
        this.history = new TranslucentRadioButton("Match History", LTextAlign.LEFT, HighlightType.TEXT);
        TranslucentButtonGroup group = new TranslucentButtonGroup();
        group.add(overview, history);

        ChildUIComponent west = new ChildUIComponent(new GridLayout(0, 2, 5, 0));
        west.add(overview);
        west.add(history);
        west.setOpaque(false);
        add(west, BorderLayout.WEST);

        ChildUIComponent east = new ChildUIComponent(new GridLayout(0, 1, 5, 0));
        search = new TranslucentTextField("Summoner Search");
        search.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        search.setBackground(new Color(0, 0, 0, 100));
        east.setOpaque(false);
        east.add(search);
        add(east, BorderLayout.EAST);
    }

    public TranslucentRadioButton getOverviewButton() {
        return overview;
    }

    public TranslucentRadioButton getHistoryButton() {
        return history;
    }

    public TranslucentTextField getSearchField() {
        return search;
    }
}
