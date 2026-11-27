package com.vuong.reader;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ReaderHubClient {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String PREFS = "readerhub_account";
    private static final String TOKEN = "token";
    private final Context context;
    private final String baseUrl;

    public ReaderHubClient(Context context) {
        this.context = context.getApplicationContext();
        this.baseUrl = this.context.getString(R.string.readerhub_base_url).trim();
    }

    public static final class User {
        public final int id;
        public final String username;
        public final String email;
        public final String role;
        User(int id, String username, String email, String role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
        }
    }

    public static final class UserList {
        public final List<User> users;
        UserList(List<User> users) { this.users = users; }
    }

    public interface Callback<T> { void onResult(T result, String error); }

    public void login(String username, String password, Callback<User> callback) {
        accountRequest("/api/mobile/login", username, "", password, callback);
    }

    public void register(String username, String email, String password, Callback<User> callback) {
        accountRequest("/api/mobile/register", username, email, password, callback);
    }

    public void currentUser(Callback<User> callback) {
        request("GET", "/api/mobile/me", null, (body, error) -> {
            if (error != null) { callback.onResult(null, error); return; }
            try { callback.onResult(parseUser(new JSONObject(body).getJSONObject("user")), null); }
            catch (Exception e) { clearToken(); callback.onResult(null, "Phiên đăng nhập đã hết hạn."); }
        });
    }

    public void logout(Callback<Boolean> callback) {
        request("POST", "/api/mobile/logout", new JSONObject(), (body, error) -> {
            clearToken();
            callback.onResult(error == null, error);
        });
    }

    public void listUsers(Callback<UserList> callback) {
        request("GET", "/api/mobile/admin/users", null, (body, error) -> {
            if (error != null) { callback.onResult(null, error); return; }
            try {
                JSONArray array = new JSONObject(body).getJSONArray("users");
                List<User> users = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) users.add(parseUser(array.getJSONObject(i)));
                callback.onResult(new UserList(users), null);
            } catch (Exception e) { callback.onResult(null, "Dữ liệu quản trị không hợp lệ."); }
        });
    }

    public void setRole(int userId, String role, Callback<User> callback) {
        JSONObject payload = new JSONObject();
        try { payload.put("role", role); } catch (Exception ignored) { }
        request("PATCH", "/api/mobile/admin/users/" + userId + "/role", payload, (body, error) -> {
            if (error != null) { callback.onResult(null, error); return; }
            try { callback.onResult(parseUser(new JSONObject(body).getJSONObject("user")), null); }
            catch (Exception e) { callback.onResult(null, "Không đọc được tài khoản sau khi cập nhật."); }
        });
    }

    public boolean hasToken() { return !token().isEmpty(); }

    private void accountRequest(String path, String username, String email, String password, Callback<User> callback) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("username", username);
            if (!email.isEmpty()) payload.put("email", email);
            payload.put("password", password);
        } catch (Exception e) { callback.onResult(null, "Không tạo được yêu cầu."); return; }
        request("POST", path, payload, (body, error) -> {
            if (error != null) { callback.onResult(null, error); return; }
            try {
                JSONObject root = new JSONObject(body);
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(TOKEN, root.getString("token")).apply();
                callback.onResult(parseUser(root.getJSONObject("user")), null);
            } catch (Exception e) { callback.onResult(null, "Phản hồi máy chủ không hợp lệ."); }
        });
    }

    private interface RawCallback { void onResult(String body, String error); }

    private void request(String method, String path, JSONObject payload, RawCallback callback) {
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(baseUrl + path);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestMethod(method);
                connection.setRequestProperty("Accept", "application/json");
                String token = token();
                if (!token.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + token);
                if (payload != null) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
                    try (OutputStream output = connection.getOutputStream()) { output.write(bytes); }
                }
                int code = connection.getResponseCode();
                InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
                String body = read(stream);
                if (code < 200 || code >= 300) {
                    try { callbackOnMain(callback, null, new JSONObject(body).optString("error", "HTTP " + code)); }
                    catch (Exception ignored) { callbackOnMain(callback, null, "HTTP " + code); }
                } else callbackOnMain(callback, body, null);
            } catch (Exception e) {
                callbackOnMain(callback, null, "Không kết nối được ReaderHub: " + e.getMessage());
            } finally { if (connection != null) connection.disconnect(); }
        });
    }

    private void callbackOnMain(RawCallback callback, String body, String error) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onResult(body, error));
    }

    private String token() { return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(TOKEN, ""); }
    private void clearToken() { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(TOKEN).apply(); }

    private static User parseUser(JSONObject object) {
        return new User(object.optInt("id"), object.optString("username", ""), object.optString("email", ""), object.optString("role", "user"));
    }

    private static String read(InputStream stream) throws Exception {
        if (stream == null) return "{}";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }
}
