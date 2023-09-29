package com.hawolt;

import com.hawolt.async.ExecutorManager;
import com.hawolt.async.gsm.ActiveGameInformation;
import com.hawolt.async.gsm.GameStartHandler;
import com.hawolt.async.presence.PresenceManager;
import com.hawolt.async.rms.GameStartListener;
import com.hawolt.async.shutdown.ShutdownManager;
import com.hawolt.authentication.LocalCookieSupplier;
import com.hawolt.cli.Argument;
import com.hawolt.cli.CLI;
import com.hawolt.cli.Parser;
import com.hawolt.cli.ParserException;
import com.hawolt.client.IClientCallback;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.RiotClient;
import com.hawolt.client.misc.ClientConfiguration;
import com.hawolt.generic.token.impl.StringTokenSupplier;
import com.hawolt.http.integrity.Diffuser;
import com.hawolt.io.JsonSource;
import com.hawolt.io.RunLevel;
import com.hawolt.logger.Logger;
import com.hawolt.manifest.RMANCache;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rtmp.amf.decoder.AMFDecoder;
import com.hawolt.ui.MainUI;
import com.hawolt.ui.chat.ChatSidebar;
import com.hawolt.ui.chat.friendlist.ChatSidebarFriendlist;
import com.hawolt.ui.chat.window.ChatUI;
import com.hawolt.ui.generic.dialog.SwiftDialog;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.WindowCloseHandler;
import com.hawolt.ui.layout.LayoutHeader;
import com.hawolt.ui.layout.LayoutManager;
import com.hawolt.ui.login.ILoginCallback;
import com.hawolt.ui.login.LoginUI;
import com.hawolt.ui.settings.SettingsUI;
import com.hawolt.util.audio.AudioEngine;
import com.hawolt.util.discord.RichPresence;
import com.hawolt.util.os.OperatingSystem;
import com.hawolt.util.os.SystemManager;
import com.hawolt.util.other.StaticConstant;
import com.hawolt.util.paint.animation.AnimationVisualizer;
import com.hawolt.util.paint.animation.impl.impl.SpinningAnimation;
import com.hawolt.util.settings.*;
import com.hawolt.virtual.client.RiotClientException;
import com.hawolt.virtual.leagueclient.exception.LeagueException;
import com.hawolt.virtual.riotclient.instance.MultiFactorSupplier;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.EventListener;
import com.hawolt.xmpp.event.EventType;
import com.hawolt.xmpp.event.objects.other.PlainData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created: 11/08/2023 18:10
 * Author: Twitter @hawolt
 **/

public class Swiftrift extends JFrame implements IClientCallback, ILoginCallback, WindowStateListener {
    public static final ExecutorService service = ExecutorManager.registerService("pool", Executors.newCachedThreadPool());
    private final LiveGameClient liveGameClient = new LiveGameClient(1000);
    private static BufferedImage logo;

    static {
        // DISABLE LOGGING USER CREDENTIALS
        StringTokenSupplier.debug = false;
        AMFDecoder.debug = false;
        try {
            logo = ImageIO.read(RunLevel.get("logo.png"));
        } catch (IOException e) {
            Logger.error("Failed to load {} logo", StaticConstant.PROJECT);
        }
    }

    private SettingService settingService;
    private ShutdownManager shutdownManager;
    private ChildUIComponent deck, main;
    private LeagueClient leagueClient;
    private PresenceManager presence;
    private ChatSidebar chatSidebar;
    private RiotClient riotClient;
    private LayoutManager manager;
    private SettingsUI settingsUI;
    private LayoutHeader headerUI;
    private LoginUI loginUI;
    private ChatUI chatUI;
    private MainUI mainUI;

    public Swiftrift(String title) {
        super(title);
        this.addWindowStateListener(this);
        this.addWindowListener(new WindowCloseHandler(this));
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void configure(boolean remember) {
        if (!remember) return;
        this.settingService.write(
                SettingType.PLAYER,
                "cookies",
                leagueClient.getVirtualRiotClientInstance().getCookieSupplier().getCurrentCookieState()
        );
    }

    private void bootstrap(LeagueClient client) {
        this.leagueClient = client;
        this.shutdownManager = new ShutdownManager(client);
        this.presence = new PresenceManager(this);
        this.configure(loginUI == null || loginUI.getRememberMe().isSelected());
        this.liveGameClient.register("GameStart", new GameStartHandler(this));
        this.dispose();
        this.loginUI.getAnimationVisualizer().stop();
        this.setUndecorated(true);
        this.setVisible(true);
        this.initialize();
        this.buildUI(leagueClient);
        this.wrap();
    }


    private void wrap() {
        this.layout.show(deck, "main");
        this.leagueClient.getRTMPClient().addDefaultCallback(presence);
        this.headerUI.getChatSidebarStatus().setPresenceManager(presence);
        this.leagueClient.getRMSClient().getHandler().addMessageServiceListener(MessageService.GSM, presence);
        this.leagueClient.getRMSClient().getHandler().addMessageServiceListener(MessageService.PARTIES, presence);
        this.leagueClient.getRMSClient().getHandler().addMessageServiceListener(MessageService.TEAMBUILDER, presence);
        this.leagueClient.getRMSClient().getHandler().addMessageServiceListener(MessageService.GSM, new GameStartListener(this));
        Swiftrift.service.execute(new ActiveGameInformation(this));
        VirtualRiotXMPPClient xmppClient = leagueClient.getXMPPClient();
        RMANCache.purge();
        this.chatUI.setSupplier(xmppClient);
        xmppClient.addHandler(
                EventType.ON_READY,
                (EventListener<PlainData>) event -> buildSidebarUI(xmppClient)
        );
        ChatSidebarFriendlist friendlist = chatSidebar.getChatSidebarFriendlist();
        xmppClient.addHandler(EventType.ON_READY, o -> presence.setIdlePresence());
        xmppClient.addMessageListener(getLayoutManager().getChampSelectUI().getChampSelect().getChampSelectDataContext().getMessageListener());
        xmppClient.addPresenceListener(friendlist);
        xmppClient.addFriendListener(friendlist);
        xmppClient.addMessageListener(chatUI);
        xmppClient.connect();
    }

    @Override
    public void onClient(LeagueClient client) {
        this.bootstrap(client);
    }

    private CardLayout layout = new CardLayout();
    private AnimationVisualizer animationVisualizer;

    private void initialize() {
        this.mainUI = new MainUI(this);
        this.deck = new ChildUIComponent(layout);
        this.animationVisualizer = new AnimationVisualizer(new SpinningAnimation(5, 45));
        this.deck.add("loading", animationVisualizer);
        this.mainUI.setMainComponent(deck);
        this.animationVisualizer.start();
    }

    private void buildUI(LeagueClient client) {
        main = new ChildUIComponent(new BorderLayout());
        deck.add("main", main);
        chatUI = new ChatUI();
        chatUI.setVisible(false);
        mainUI.addChatComponent(chatUI);
        settingsUI = new SettingsUI(this);
        settingsUI.setVisible(false);
        mainUI.addSettingsComponent(settingsUI);
        chatSidebar = new ChatSidebar(this);
        manager = new LayoutManager(this);
        main.add(manager, BorderLayout.CENTER);
        main.add(chatSidebar, BorderLayout.EAST);
        main.add(headerUI = new LayoutHeader(manager, this), BorderLayout.NORTH);
        manager.setHeader(headerUI);
        mainUI.revalidate();
    }

    private void buildSidebarUI(VirtualRiotXMPPClient xmppClient) {
        ChatSidebarFriendlist friendlist = chatSidebar.getChatSidebarFriendlist();
        Swiftrift.service.execute(() -> {
            friendlist.onEvent(xmppClient.getFriendList());
            friendlist.revalidate();
        });
    }

    public LeagueClient getLeagueClient() {
        return leagueClient;
    }

    public LayoutManager getLayoutManager() {
        return manager;
    }

    public ChatSidebar getChatSidebar() {
        return chatSidebar;
    }

    public RiotClient getRiotClient() {
        return riotClient;
    }

    public LayoutManager getManager() {
        return manager;
    }

    public LayoutHeader getHeader() {
        return headerUI;
    }

    public ChatUI getChatUI() {
        return chatUI;
    }

    public SettingsUI getSettingsUI() {
        return settingsUI;
    }

    public LoginUI getLoginUI() {
        return loginUI;
    }

    public MainUI getMainUI() {
        return mainUI;
    }

    public SettingService getSettingService() {
        return settingService;
    }

    public ShutdownManager getShutdownManager() {
        return shutdownManager;
    }

    private void showFailureDialog(String message) {
        Swiftrift.showMessageDialog(message);
    }

    @Override
    public void onLoginFlowException(Throwable throwable) {
        Logger.error("Failed to initialize Client");
        Logger.error(throwable);
        loginUI.toggle(true);
        if (throwable instanceof RiotClientException e) {
            switch (e.getMessage()) {
                case "ERROR_TYPE_IS_NULL" -> showFailureDialog("Login errored but the error returned is null");
                case "CAPTCHA_NOT_SUCCESSFUL" -> showFailureDialog("Our Captcha was denied");
                case "UNKNOWN_RESPONSE" -> showFailureDialog("Unable to tell what is wrong");
                case "MISSING_TYPE" -> showFailureDialog("Unable to tell login stage type");
                case "AUTH_FAILURE" -> showFailureDialog("Invalid username or password");
                case "RATE_LIMITED" -> showFailureDialog("You are being rate limited");
                case "UNKNOWN" -> showFailureDialog("If you see this Riot is burning");
                case "CLOUDFLARE" -> showFailureDialog("Temporary Cloudflare block");
                default -> showFailureDialog("Unhandled RiotClientException");
            }
        } else if (throwable instanceof LeagueException e) {
            switch (e.getType()) {
                case NO_LEAGUE_ACCOUNT -> showFailureDialog("No League account connected");
                case NO_SUMMONER_NAME -> showFailureDialog("No name set for summoner");
                default -> showFailureDialog("Unhandled LeagueClientException");
            }
        } else if (throwable instanceof IOException) {
            switch (throwable.getMessage()) {
                case "PREFERENCE_FAILURE" -> showFailureDialog("Unable to load Player Preference");
                default -> showFailureDialog("Unhandled IOException");
            }
        } else {
            showFailureDialog("Unknown Error during login");
        }
    }

    private ClientConfiguration getConfiguration(String username, String password) {
        return ClientConfiguration.getDefault(username, password, new MultiFactorSupplier() {
            @Override
            public String get() {
                return Swiftrift.showInputDialog("Enter 2FA Code");
            }
        });
    }

    @Override
    public void onLogin(String username, String password) {
        if (loginUI.getRememberMe().isSelected()) {
            settingService.write(SettingType.CLIENT, "remember", true);
            settingService.write(SettingType.CLIENT, "username", username);
        }
        this.settingService.set(username);
        this.createRiotClient(getConfiguration(username, password));
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        if (e.getNewState() == Frame.MAXIMIZED_BOTH) {
            mainUI.adjust();
        }
    }

    private void createRiotClient(ClientConfiguration configuration) {
        this.riotClient = new RiotClient(configuration, this);
    }

    private static void printLaunchDetail(String[] args) {
        for (int i = 0; i < args.length; i++) {
            Logger.info("{}: {}", i, args[i]);
        }
        try {
            JsonSource source = JsonSource.of(RunLevel.get(StaticConstant.PROJECT_DATA));
            StaticConstant.VERSION = source.get("version");
            Logger.info("Running Swiftrift-{}", StaticConstant.VERSION);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private static void handleCommandLine(String[] args) {
        Parser parser = new Parser();
        parser.add(Argument.create("p", "privacy", "disable privacy enhancement", false, true, true));
        try {
            CLI cli = parser.check(args);
            if (cli.has("privacy")) {
                Diffuser.PRIVACY_ENHANCEMENT = false;
            }
        } catch (ParserException e) {
            System.err.println(parser.getHelp());
        }
    }

    public static void main(String[] args) {
        printLaunchDetail(args);
        handleCommandLine(args);
        RMANCache.preload();
        AudioEngine.install();
        Swiftrift.service.execute(() -> {
            String processName = switch (OperatingSystem.getOperatingSystemType()) {
                case MAC -> "Discord.app";
                case LINUX -> "Discord";
                case WINDOWS -> "Discord.exe";
                default -> null;
            };
            try {
                if (processName == null || !SystemManager.getInstance().isProcessRunning(processName)) return;
                RichPresence.show();
            } catch (IOException e) {
                Logger.error(e);
            }
        });
        Swiftrift swiftrift = new Swiftrift(StaticConstant.PROJECT);
        swiftrift.setIconImage(logo);
        swiftrift.settingService = new SettingManager();
        swiftrift.loginUI = LoginUI.create(swiftrift);
        ClientSettings clientSettings = swiftrift.settingService.getClientSettings();
        swiftrift.loginUI.getRememberMe().setSelected(clientSettings.isRememberMe());
        swiftrift.loginUI.toggle(false);
        LocalCookieSupplier localCookieSupplier = new LocalCookieSupplier();
        if (clientSettings.isRememberMe()) {
            UserSettings userSettings = swiftrift.settingService.set(clientSettings.getRememberMeUsername());
            localCookieSupplier.loadCookieState(userSettings.getCookies());
            if (localCookieSupplier.isInCompletedState()) {
                ClientConfiguration configuration = ClientConfiguration.getDefault(localCookieSupplier);
                swiftrift.createRiotClient(configuration);
            }
        }
        swiftrift.loginUI.toggle(true);
        swiftrift.setVisible(true);
    }

    private long lastFocusRequest;

    public void focus() {
        if (System.currentTimeMillis() - lastFocusRequest <= TimeUnit.MINUTES.toMillis(1)) return;
        if (isActive() && hasFocus()) return;
        this.toFront();
        this.setExtendedState(JFrame.ICONIFIED);
        this.setExtendedState(JFrame.NORMAL);
        this.lastFocusRequest = System.currentTimeMillis();
    }

    public static int showMessageDialog(String... lines) {
        return SwiftDialog.showMessageDialog(Frame.getFrames()[0], lines);
    }

    public static int showOptionDialog(String message, String... options) {
        return SwiftDialog.showOptionDialog(Frame.getFrames()[0], message, options);
    }

    public static int showOptionDialog(String[] messages, String... options) {
        return SwiftDialog.showOptionDialog(Frame.getFrames()[0], messages, options);
    }

    public static String showInputDialog(String message) {
        return SwiftDialog.showInputDialog(Frame.getFrames()[0], message);
    }
}
