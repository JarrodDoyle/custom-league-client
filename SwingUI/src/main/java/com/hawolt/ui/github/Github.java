package com.hawolt.ui.github;

import com.hawolt.generic.util.Network;
import com.hawolt.http.NativeHttpClient;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import com.hawolt.virtual.misc.DynamicObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

public class Github {

    private static final String BASE_URL = "https://api.github.com/repos/hawolt/custom-league-client";

    private static final String BUG_URL = "https://github.com/hawolt/custom-league-client/issues/new?assignees=&labels=bug&projects=&template=bug_report.md&title=%5BBUG%5D";

    private static JSONObject latestRelease;
    private static JSONArray releases;
    private static JSONArray contributorsList;
    private static JSONArray issues;

    private Github() {
    }

    public static DynamicObject getLatestRelease() {
        String url = BASE_URL + "/releases/latest";

        if (latestRelease == null) {
            try {
                IResponse response = doRequest(url);
                latestRelease = response.code() == 200 ? new JSONObject(response.asString()) : new JSONObject();
            } catch (IOException | InterruptedException e) {
                latestRelease = new JSONObject();
            }
        }

        return new DynamicObject(latestRelease);
    }

    public static JSONArray getReleases() {
        String url = BASE_URL + "/releases";

        if (releases == null) {
            try {
                IResponse response = doRequest(url);
                releases = response.code() == 200 ? new JSONArray(response.asString()) : new JSONArray();
            } catch (IOException | InterruptedException e) {
                releases = new JSONArray();
            }
        }

        return releases;
    }

    public static JSONArray getContributorsList() {
        String url = BASE_URL + "/contributors";

        if (contributorsList == null) {
            try {
                IResponse response = doRequest(url);
                contributorsList = response.code() == 200 ? new JSONArray(response.asString()) : new JSONArray();
            } catch (IOException | InterruptedException e) {
                contributorsList = new JSONArray();
            }
        }

        return contributorsList;
    }

    public static JSONArray getIssues() {
        String url = BASE_URL + "/issues";

        if (issues == null) {
            try {
                IResponse response = doRequest(url);
                issues = response.code() == 200 ? new JSONArray(response.asString()) : new JSONArray();
            } catch (IOException | InterruptedException e) {
                issues = new JSONArray();
            }
        }

        return issues;
    }

    private static IResponse doRequest(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        return NativeHttpClient.execute(request);
    }

    private static DynamicObject getVersion() {
        JSONObject currentVersion = new JSONObject();
        JSONArray versions = getReleases();
        Properties props = new Properties();
        try {
            props.load(Github.class.getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String version = props.getProperty("version");
        version = version.replace("-", "-release-");
        for (int i = 0; i < versions.length(); i++) {
            if (version.equals(versions.getJSONObject(i).get("name")))
                currentVersion = versions.getJSONObject(i);
        }
        return new DynamicObject(currentVersion);
    }

    public static String getChangelog() {
        return getVersion().getByKeyOrDefault("body", "RATE-LIMITED").toString();
    }

    public static String getCurrentVersion() {
        return getVersion().getByKeyOrDefault("name", "RATE-LIMITED").toString();
    }

    public static String getLatestVersion() {
        return getLatestRelease().getByKeyOrDefault("name", "RATE-LIMITED").toString();
    }

    public static String getCurrentReleaseDate() {
        return date(getVersion().getByKeyOrDefault("published_at", null));
    }

    public static String getLatestReleaseDate() {
        return date(getLatestRelease().getByKeyOrDefault("published_at", null));
    }

    public static void submitBug() {
        try {
            Network.browse(BUG_URL);
        } catch (IOException e) {
            Logger.error("failed to request bug submit");
        }
    }

    private static String date(String date) {
        if (date == null) return Date.from(Instant.now()).toString();
        Date release = Date.from(Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(date)));
        return release.toString();
    }
}
