package com.vuong.reader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReaderHubActivity extends Activity {
    private ReaderHubClient client;
    private LinearLayout root;
    private TextView message;
    private TextView accountStatus;
    private EditText username;
    private EditText email;
    private EditText password;
    private boolean registerMode;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        client = new ReaderHubClient(this);
        showAuth();
        if (client.hasToken()) loadCurrentUser();
    }

    private void showAuth() {
        root = column();
        TextView title = text("ReaderHub", 24);
        root.addView(title);
        root.addView(text("Tài khoản máy chủ và quyền quản trị được kết nối qua HTTPS.", 16));
        accountStatus = text("Chưa đăng nhập", 15);
        accountStatus.setPadding(0, 18, 0, 12);
        root.addView(accountStatus);
        username = field("Tên người dùng");
        password = field("Mật khẩu");
        password.setInputType(0x00000081);
        root.addView(username);
        email = field("Email khi đăng ký");
        root.addView(email);
        root.addView(password);
        Button submit = button("Đăng nhập");
        submit.setOnClickListener(v -> submit(submit));
        root.addView(submit);
        Button toggle = button("Chuyển sang đăng ký");
        toggle.setOnClickListener(v -> { registerMode = !registerMode; email.setVisibility(registerMode ? View.VISIBLE : View.GONE); submit.setText(registerMode ? "Đăng ký" : "Đăng nhập"); toggle.setText(registerMode ? "Đã có tài khoản? Đăng nhập" : "Chuyển sang đăng ký"); });
        root.addView(toggle);
        message = text("", 14);
        message.setPadding(0, 12, 0, 0);
        root.addView(message);
        setContentView(root);
        email.setVisibility(View.GONE);
    }

    private void submit(Button submit) {
        String user = username.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String pass = password.getText().toString();
        if (user.length() < 3 || pass.length() < 8 || (registerMode && mail.length() < 5)) { message.setText("Vui lòng nhập đúng tên người dùng, mật khẩu và email."); return; }
        submit.setEnabled(false);
        ReaderHubClient.Callback<ReaderHubClient.User> callback = (result, error) -> {
            submit.setEnabled(true);
            if (error != null) { message.setText(error); return; }
            showAccount(result);
        };
        if (registerMode) client.register(user, mail, pass, callback); else client.login(user, pass, callback);
    }

    private void loadCurrentUser() {
        client.currentUser((user, error) -> { if (user != null) showAccount(user); else message.setText(error == null ? "Chưa đăng nhập." : error); });
    }

    private void showAccount(ReaderHubClient.User user) {
        root.removeAllViews();
        root.addView(text("ReaderHub", 24));
        accountStatus = text("Đã kết nối máy chủ", 16);
        root.addView(accountStatus);
        root.addView(text("Tên: " + user.username + "\nEmail: " + user.email + "\nVai trò: " + roleLabel(user.role), 16));
        Button logout = button("Đăng xuất ReaderHub");
        logout.setOnClickListener(v -> client.logout((ok, error) -> showAuth()));
        root.addView(logout);
        message = text("", 14);
        root.addView(message);
        if ("admin".equals(user.role) || "developer".equals(user.role)) {
            root.addView(text("Khu vực quản trị", 20));
            loadUsers(user);
        }
        Button back = button("Quay lại");
        back.setOnClickListener(v -> finish());
        root.addView(back);
    }

    private void loadUsers(ReaderHubClient.User current) {
        client.listUsers((list, error) -> {
            if (error != null) { message.setText(error); return; }
            for (ReaderHubClient.User account : list.users) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                TextView info = text(account.username + " — " + roleLabel(account.role), 14);
                row.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
                if ("developer".equals(current.role) && !"developer".equals(account.role)) {
                    Button action = button("admin".equals(account.role) ? "Hạ chức" : "Thăng chức");
                    action.setOnClickListener(v -> client.setRole(account.id, "admin".equals(account.role) ? "user" : "admin", (updated, updateError) -> { if (updateError != null) message.setText(updateError); else { message.setText("Đã cập nhật vai trò."); showAccount(current); } }));
                    row.addView(action);
                }
                root.addView(row);
            }
        });
    }

    private LinearLayout column() { LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(32, 32, 32, 32); return layout; }
    private TextView text(String value, float size) { TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setPadding(0, 8, 0, 8); return view; }
    private EditText field(String hint) { EditText input = new EditText(this); input.setHint(hint); input.setSingleLine(true); input.setTextSize(16); input.setPadding(0, 12, 0, 12); return input; }
    private Button button(String label) { Button button = new Button(this); button.setText(label); return button; }
    private String roleLabel(String role) { return "developer".equals(role) ? "Nhà phát triển gốc" : "admin".equals(role) ? "Quản trị viên" : "Người dùng"; }
}
