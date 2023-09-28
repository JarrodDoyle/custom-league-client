package com.hawolt.ui.settings;

import com.hawolt.Swiftrift;
import com.hawolt.ui.generic.component.LComboBox;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.util.os.OperatingSystem;
import com.hawolt.util.settings.SettingService;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingsUI extends ChildUIComponent {
    private static final Font font = new Font("Arial", Font.PLAIN, 20);
    private final List<SettingsPage> pages = new ArrayList<>();
    private final Swiftrift swiftrift;
    private final SettingsSidebar sidebar;

    public SettingsUI(Swiftrift swiftrift) {
        super(new BorderLayout());
        this.swiftrift = swiftrift;
        setBorder(BorderFactory.createTitledBorder(
                        new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY)
                )
        );
        ChildUIComponent header = new ChildUIComponent(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 40));
        JLabel label = new JLabel("Settings");
        label.setFont(font);
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(JLabel.CENTER);
        header.add(label, BorderLayout.CENTER);

        //CardLayout
        CardLayout cl = new CardLayout();
        JPanel mainPanel = new JPanel(cl);
        SettingsPage clientGeneralPage = newClientGeneralPage();
        SettingsPage themePage = newThemePage();
        SettingsPage clientAudioPage = newClientAudioPage();
        SettingsPage clientAboutPage = newClientAboutPage();
        SettingsPage clientHelpPage = newClientHelpPage();
        mainPanel.add("General", clientGeneralPage);
        mainPanel.add("Client Theme", themePage);
        mainPanel.add("Audio", clientAudioPage);
        mainPanel.add("About", clientAboutPage);
        mainPanel.add("Help", clientHelpPage);
        for (int i = 0; i < mainPanel.getComponentCount(); i++) {
            pages.add((SettingsPage) mainPanel.getComponent(i));
        }
        add(mainPanel);

        add(header, BorderLayout.NORTH);

        //Sidebar
        sidebar = new SettingsSidebar();
        add(sidebar, BorderLayout.WEST);

        SettingsSidebar.GroupTab clientGroup = sidebar.addGroupTab("Client");
        SettingsSidebar.GroupTab aboutGroup = sidebar.addGroupTab("About");

        LFlatButton clientGeneralButton = SettingsSidebar.newSectionButton("General", cl, mainPanel);
        clientGroup.addToContainer(clientGeneralButton);
        LFlatButton themeButton = SettingsSidebar.newSectionButton("Client Theme", cl, mainPanel);
        clientGroup.addToContainer(themeButton);
        LFlatButton clientAudioButton = SettingsSidebar.newSectionButton("Audio", cl, mainPanel);
        clientGroup.addToContainer(clientAudioButton);
        LFlatButton clientAboutButton = SettingsSidebar.newSectionButton("About", cl, mainPanel);
        aboutGroup.addToContainer(clientAboutButton);
        LFlatButton clientHelpButton = SettingsSidebar.newSectionButton("Help", cl, mainPanel);
        aboutGroup.addToContainer(clientHelpButton);

        //Footer
        ChildUIComponent footer = new ChildUIComponent(new FlowLayout(FlowLayout.CENTER, 5, 5));
        add(footer, BorderLayout.SOUTH);

        LFlatButton saveButton = new LFlatButton("Save", LTextAlign.CENTER, HighlightType.COMPONENT);
        saveButton.addActionListener(listener -> {
            save();
        });
        footer.add(saveButton);

        LFlatButton closeButton = new LFlatButton("Close", LTextAlign.CENTER, HighlightType.COMPONENT);
        closeButton.addActionListener(listener -> {
            cl.first(mainPanel);
            close();
        });
        footer.add(closeButton);
        revalidate();
    }

    public void save() {
        for (SettingsPage page : pages) {
            page.save();
        }
    }

    public void close() {
        for (SettingsPage page : pages) {
            page.close();
        }
        this.setVisible(false);
    }

    public void add(SettingsPage page) {
        pages.add(page);
        add(page, BorderLayout.CENTER);
    }

    private SettingsPage newClientGeneralPage() {
        SettingService service = swiftrift.getSettingService();
        SettingsPage result = new SettingsPage();
        result.add(SettingUIComponent.createTagComponent("Path"));
        String defaultGameBase = OperatingSystem.getOperatingSystemType() == OperatingSystem.OSType.WINDOWS ?
                String.join(File.separator, "C:", "Riot Games", "League of Legends") :
                "";
        result.add(SettingUIComponent.createPathComponent("League Base Directory Path", service, "GameBaseDir", defaultGameBase));
        if (OperatingSystem.getOperatingSystemType() == OperatingSystem.OSType.LINUX) {
            result.add(SettingUIComponent.createPathComponent("Wine Prefix Path", service, "WinePrefixDir", ""));
            result.add(SettingUIComponent.createPathComponent("Wine Binary Path (wine64)", service, "WineBinaryDir", ""));
        }
        result.add(SettingUIComponent.createTagComponent("Friend requests"));
        result.add(SettingUIComponent.createAutoFriendComponent("Auto friend request handling", service, "autoFriends"));
        if (OperatingSystem.getOperatingSystemType() == OperatingSystem.OSType.LINUX) {
            result.add(SettingUIComponent.createTagComponent("Linux Launch Options"));
            result.add(SettingUIComponent.createCheckBoxComponent("MangoHUD", service, "mangoHud", false));
            result.add(SettingUIComponent.createCheckBoxComponent("GameMode", service, "gameModeRun", false));
        }
        return result;
    }

    private SettingsPage newThemePage() {
        SettingService service = swiftrift.getSettingService();
        SettingsPage result = new SettingsPage();

        LComboBox<LThemeChoice> comboBox = new LComboBox<>(LThemeChoice.values());
        comboBox.addItemListener(listener -> {
            ColorPalette.setTheme(comboBox.getItemAt(comboBox.getSelectedIndex()));
        });
        comboBox.setSelectedIndex(service.getClientSettings().getClientTheme());
        //ColorPalette.setTheme();

        result.add(SettingUIComponent.createTagComponent("Theme"));


        SettingUIComponent themeCombo = SettingUIComponent.createComboBoxComponent("Client Theme", service, "Theme", comboBox);

        result.add(themeCombo);
        return result;
    }

    private SettingsPage newClientAudioPage() {
        SettingService service = swiftrift.getSettingService();
        SettingsPage result = new SettingsPage();
        result.add(SettingUIComponent.createTagComponent("Volume"));
        result.add(SettingUIComponent.createVolumeComponent("Client Master Volume", service, "Volume", "MixerVolume"));
        return result;
    }

    private SettingsPage newClientAboutPage() {
        SettingsPage result = new SettingsPage();
        result.add(SettingUIComponent.createTagComponent("About"));
        result.add(SettingUIComponent.createAboutComponent("Swift-Rift-Crew"));
        return result;
    }

    private SettingsPage newClientHelpPage() {
        SettingsPage result = new SettingsPage();
        result.add(SettingUIComponent.createTagComponent("Need help?"));
        result.add(SettingUIComponent.createHelpComponent());
        result.add(SettingUIComponent.createTagComponent("Bugs"));
        result.add(SettingUIComponent.createKnownBugComponent());
        result.add(SettingUIComponent.createSubmitBugComponent());
        return result;
    }
}
