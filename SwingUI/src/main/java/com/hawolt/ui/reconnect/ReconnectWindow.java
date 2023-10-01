package com.hawolt.ui.reconnect;

import com.hawolt.Swiftrift;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.ui.layout.LayoutComponent;
import com.hawolt.util.other.Launcher;

import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReconnectWindow extends ChildUIComponent {

    public ReconnectWindow(Swiftrift swiftrift) {
        super(new BorderLayout());
        LFlatButton button = new LFlatButton("Reconnect", LTextAlign.CENTER, HighlightType.COMPONENT);
        button.addActionListener(e -> {
            Launcher.launch(swiftrift.getSettingService(), swiftrift.getLeagueClient().getPlayerPlatform(), swiftrift.getLeagueClient().getCachedValue(CacheElement.GAME_CREDENTIALS));
            swiftrift.getLayoutManager().getHeader().selectAndShowComponent(LayoutComponent.HOME);
            swiftrift.getLayoutManager().getHeader().hide(LayoutComponent.RECONNECT);
        });
        add(button);
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }
}
