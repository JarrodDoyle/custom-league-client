package com.hawolt.ui.layout;

import com.hawolt.Swiftrift;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.cache.CacheElement;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.ui.chat.profile.ChatSidebarProfile;
import com.hawolt.ui.chat.profile.ChatSidebarStatus;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.ui.generic.utility.LazyLoadedImageComponent;
import com.hawolt.ui.layout.wallet.HeaderWallet;
import com.hawolt.util.os.OperatingSystem;
import com.hawolt.util.settings.SettingService;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 09/08/2023 15:52
 * Author: Twitter @hawolt
 **/

public class LayoutHeader extends ChildUIComponent {
    private final Map<LayoutComponent, LFlatButton> map = new HashMap<>();
    private final Swiftrift swiftrift;
    private final ChatSidebarProfile profile;
    private final ILayoutManager manager;
    private final HeaderWallet wallet;

    private Point initialClick;

    public LayoutHeader(ILayoutManager manager, Swiftrift swiftrift) {
        super(new BorderLayout());
        this.manager = manager;
        this.swiftrift = swiftrift;
        this.setBackground(ColorPalette.backgroundColor);
        this.setPreferredSize(new Dimension(0, 90));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                LayoutHeader.this.initialClick = e.getPoint();
            }
        });
        Frame source = Frame.getFrames()[0];
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                final int thisX = source.getLocation().x;
                final int thisY = source.getLocation().y;
                final int xMoved = e.getX() - LayoutHeader.this.initialClick.x;
                final int yMoved = e.getY() - LayoutHeader.this.initialClick.y;
                final int X = thisX + xMoved;
                final int Y = thisY + yMoved;
                source.setLocation(X, Y);
            }
        });

        ChildUIComponent main = new ChildUIComponent(new BorderLayout());
        main.setBorder(new EmptyBorder(5, 5, 5, 5));
        main.setBackground(ColorPalette.backgroundColor);
        add(main, BorderLayout.CENTER);

        LazyLoadedImageComponent component = new LazyLoadedImageComponent(new Dimension(80, 80), 5);
        component.setBackground(ColorPalette.backgroundColor);
        ResourceLoader.loadLocalResource("fullsize-logo.png", component);
        add(component, BorderLayout.WEST);

        ChildUIComponent verticalButtonAlignment = new ChildUIComponent();
        verticalButtonAlignment.setBackground(ColorPalette.backgroundColor);
        verticalButtonAlignment.setLayout(new BoxLayout(verticalButtonAlignment, BoxLayout.X_AXIS));
        main.add(verticalButtonAlignment, BorderLayout.WEST);

        for (LayoutComponent layoutComponent : LayoutComponent.values()) {
            verticalButtonAlignment.add(Box.createRigidArea(new Dimension(10, 0)));
            verticalButtonAlignment.add(createHeaderComponent(layoutComponent));
        }
        selectAndShowComponent(LayoutComponent.HOME);

        main.add(wallet = new HeaderWallet(swiftrift.getLeagueClient()), BorderLayout.EAST);
        Summoner summoner = swiftrift.getLeagueClient().getCachedValue(CacheElement.SUMMONER);
        UserInformation userInformation = swiftrift.getLeagueClient().getVirtualLeagueClient()
                .getVirtualLeagueClientInstance()
                .getUserInformation();
        add(profile = new ChatSidebarProfile(userInformation, summoner), BorderLayout.EAST);
        configure(userInformation);
    }

    public LFlatButton createHeaderComponent(LayoutComponent component) {
        LFlatButton button = new LFlatButton(component.name().replace("_", " "), LTextAlign.CENTER, HighlightType.TEXT);
        button.addActionListener(listener -> selectAndShowComponent(component));
        map.put(component, button);
        return button;
    }

    public void selectAndShowComponent(LayoutComponent component) {
        if (component != LayoutComponent.PLAY || OperatingSystem.getOperatingSystemType() == OperatingSystem.OSType.WINDOWS) {
            selectAndShow(component);
        } else {
            SettingService service = swiftrift.getSettingService();
            String gameBaseDir = service.getClientSettings().getByKeyOrDefault("GameBaseDir", null);
            String winePrefixDirectory = service.getClientSettings().getByKeyOrDefault("WinePrefixDir", null);
            String wineBinaryDirectory = service.getClientSettings().getByKeyOrDefault("WineBinaryDir", null);
            boolean isLinux = OperatingSystem.getOperatingSystemType() == OperatingSystem.OSType.LINUX;
            if (gameBaseDir == null || (isLinux && (winePrefixDirectory == null || wineBinaryDirectory == null))) {
                Swiftrift.showMessageDialog("Please configure your Settings first");
                return;
            }
            selectAndShow(component);
        }
    }

    private void selectAndShow(LayoutComponent component) {
        map.values().forEach(button -> button.setSelected(false));
        manager.showComponent(component.toString());
        LFlatButton button = map.get(component);
        button.setSelected(true);
    }

    public void configure(UserInformation userInformation) {
        if (userInformation.isLeagueAccountAssociated()) {
            String name = userInformation.getUserInformationLeagueAccount().getSummonerName();
            getProfile().getChatSidebarName().setSummonerName(name);
            long iconId = userInformation.getUserInformationLeagueAccount().getProfileIcon();
            getProfile().getIcon().setIconId(iconId);
        } else {
            getProfile().getChatSidebarName().setSummonerName("");
            getProfile().getIcon().setIconId(29);
        }
    }

    public ChatSidebarProfile getProfile() {
        return profile;
    }


    public HeaderWallet getWallet() {
        return wallet;
    }

    public ChatSidebarStatus getChatSidebarStatus() {
        return profile.getStatus();
    }

    public String getSelectedStatus() {
        return profile.getStatus().getSelectedStatus();
    }
}
