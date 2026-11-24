package com.vuong.reader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class UpdateChecker {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private UpdateChecker() { }

    public static final class UpdateInfo {
        public final int versionCode;
        public final String versionName;
        public final String downloadUrl;
        public final String releaseNotes;
        UpdateInfo(int versionCode, String versionName, String downloadUrl, String releaseNotes) {
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.downloadUrl = downloadUrl;
            this.releaseNotes = releaseNotes;
        }
    }

    public interface Callback { void onResult(UpdateInfo info, Exception error); }

    public static void check(Context context, Callback callback) {
        String endpoint = context.getString(R.string.update_server_url).trim();
        if (endpoint.length() == 0) {
            callback.onResult(null, new IllegalStateException("update endpoint is not configured"));
            return;
        }
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300)
                    throw new IllegalStateException("HTTP " + connection.getResponseCode());
                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                }
                JSONObject root = new JSONObject(body.toString());
                JSONObject latest = root.optJSONObject("latest");
                UpdateInfo result = latest == null ? null : new UpdateInfo(
                        latest.optInt("versionCode", 0),
                        latest.optString("versionName", ""),
                        latest.optString("downloadUrl", ""),
                        latest.optString("releaseNotes", ""));
                post(context, () -> callback.onResult(result, null));
            } catch (Exception error) {
                post(context, () -> callback.onResult(null, error));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static void post(Context context, Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }
}
