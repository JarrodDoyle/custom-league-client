package com.hawolt.util.other;

import com.hawolt.generic.data.Platform;
import com.hawolt.logger.Logger;
import com.hawolt.util.os.OperatingSystem;
import com.hawolt.util.settings.SettingService;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: 10/08/2023 21:11
 * Author: Twitter @hawolt
 **/

public class Launcher {
    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();

    public static void launch(SettingService service, String ip, String port, String encryptionKey, String playerId, String gameId, Platform platform, String gameMode) {
        String defaultGameBase = OperatingSystem.getOperatingSystemType() == OperatingSystem.OSType.WINDOWS ?
                String.join(File.separator, "C:", "Riot Games", "League of Legends") :
                "";
        String leagueDirectory = service.getClientSettings().getByKeyOrDefault(
                "GameBaseDir",
                defaultGameBase
        );
        SERVICE.execute(() -> {
            try {
                ProcessBuilder builder = new ProcessBuilder();
                builder.directory(new File(String.join(File.separator, leagueDirectory, "Game")));
                builder.redirectErrorStream(true);

                List<String> command = new ArrayList<>();
                Map<String, String> env = builder.environment();

                if (OperatingSystem.getOperatingSystemType() == OperatingSystem.OSType.LINUX) {
                    String winePrefixDirectory = service.getClientSettings().getWinePrefixDirectory();
                    String wineBinaryDirectory = service.getClientSettings().getWineBinaryDirectory();
                    env.put("PATH", String.join(":", System.getenv().get("PATH"), wineBinaryDirectory));
                    env.put("WINELOADER", String.join(File.separator, wineBinaryDirectory, "wine64"));
                    env.put("WINEPREFIX", winePrefixDirectory);
                    env.put("WINEESYNC", "1");
                    env.put("WINEFSYNC", "1");
                    env.put("WINEDEBUG", "-all");
                    env.put("WINEDLLOVERRIDES", "dxgi,d3d9,d3d10core,d3d11=n;winemenubuilder.exe=d;mscoree,mshtml=");
                    if (service.getClientSettings().isMangoHudEnabled()) {
                        env.put("MANGOHUD", "1");
                    }
                    if (service.getClientSettings().isGameModeEnabled()) {
                        command.add("gamemoderun");
                    }
                    command.add(builder.environment().get("WINELOADER"));
                }

                command.addAll(
                        Arrays.asList(
                                String.join(File.separator, leagueDirectory, "Game", "League of Legends.exe"),
                                String.format("%s %s %s %s", ip, port, encryptionKey, playerId),
                                "-Product=" + ("TFT".equals(gameMode) ? gameMode : "LoL"),
                                "-PlayerID=" + playerId,
                                "-GameID=" + gameId,
                                "-PlayerNameMode=SUMMONER",
                                "-GameBaseDir=" + leagueDirectory,
                                "-Region=" + platform.getFriendlyName(),
                                "-PlatformID=" + platform.name(),
                                "-Locale=en_US",
                                "-SkipBuild",
                                "-EnableCrashpad=true",
                                "-EnableLNP",
                                "-UseDX11=1:1",
                                "-UseMetal=0:1",
                                "-UseNewX3D",
                                "-UseNewX3DFramebuffers",
                                "-RiotClientPort=42069",
                                "-RiotClientAuthToken=SwiftRiftOrNoRiftAtAll"
                        )
                );
                builder.command(command);
                Process process = builder.start();
                try (FileWriter writer = new FileWriter("log", false)) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = in.readLine()) != null) {
                            Logger.debug("[game-process] {}", line);
                            writer.write(line + System.lineSeparator());
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        });
    }

    public static void launch(SettingService service, Platform platform, JSONObject object) {
        String ip = object.getString("serverIp");
        String gameMode = object.getString("gameMode");
        String port = String.valueOf(object.getInt("serverPort"));
        String encryptionKey = object.getString("encryptionKey");
        String gameId = String.valueOf(object.getLong("gameId"));
        String summonerId = String.valueOf(object.getLong("summonerId"));
        Launcher.launch(service, ip, port, encryptionKey, summonerId, gameId, platform, gameMode);
    }
}
