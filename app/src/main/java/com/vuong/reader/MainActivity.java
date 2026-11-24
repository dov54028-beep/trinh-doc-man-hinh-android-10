package com.vuong.reader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int CURRENT_VERSION_CODE = 3;
    private TextView status;
    private TextView updateStatus;
    private Button downloadUpdate;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        TextView title = new TextView(this);
        title.setText(getString(R.string.app_name));
        title.setTextSize(24);
        root.addView(title);
        status = new TextView(this);
        status.setTextSize(17);
        status.setPadding(0, 12, 0, 12);
        root.addView(status);
        Button settings = new Button(this);
        settings.setText(R.string.open_settings);
        settings.setOnClickListener(v -> startActivity(new Intent(this, ReaderSettingsActivity.class)));
        root.addView(settings);
        Button accessibility = new Button(this);
        accessibility.setText(R.string.open_accessibility);
        accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(accessibility);
        Button test = new Button(this);
        test.setText(R.string.test_tts);
        test.setOnClickListener(v -> ScreenReaderService.speakFromTest("Đây là bản thử nghiệm của trình đọc màn hình Android 10."));
        root.addView(test);
        updateStatus = new TextView(this);
        updateStatus.setText(R.string.update_checking);
        updateStatus.setTextSize(16);
        updateStatus.setPadding(0, 24, 0, 6);
        root.addView(updateStatus);
        downloadUpdate = new Button(this);
        downloadUpdate.setText(R.string.update_available);
        downloadUpdate.setVisibility(View.GONE);
        root.addView(downloadUpdate);
        setContentView(root);
        checkForUpdates();
    }

    private void checkForUpdates() {
        UpdateChecker.check(this, (info, error) -> {
            if (error != null || info == null) {
                updateStatus.setText(getString(R.string.update_unavailable));
                return;
            }
            if (info.versionCode > CURRENT_VERSION_CODE && info.downloadUrl.length() > 0) {
                updateStatus.setText(getString(R.string.update_available, info.versionName));
                downloadUpdate.setText(getString(R.string.update_available, info.versionName));
                downloadUpdate.setVisibility(View.VISIBLE);
                downloadUpdate.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))));
            } else updateStatus.setText(getString(R.string.update_latest));
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (status != null) status.setText(ScreenReaderService.isRunning() ? R.string.status_on : R.string.status_off);
    }
}
