package com.vuong.reader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class ReaderSettingsActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        TextView title = new TextView(this);
        title.setText(R.string.settings_title);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title);
        TextView info = new TextView(this);
        info.setText(R.string.settings_info);
        info.setTextSize(17);
        info.setPadding(0, 24, 0, 0);
        root.addView(info);
        Switch earcons = new Switch(this);
        earcons.setText(R.string.settings_earcons);
        earcons.setChecked(getSharedPreferences("reader", MODE_PRIVATE).getBoolean("earcons", true));
        earcons.setOnCheckedChangeListener((button, checked) -> getSharedPreferences("reader", MODE_PRIVATE)
                .edit().putBoolean("earcons", checked).apply());
        root.addView(earcons);
        Switch compact = new Switch(this);
        compact.setText(R.string.settings_compact_speech);
        compact.setChecked(getSharedPreferences("reader", MODE_PRIVATE).getBoolean("compactSpeech", false));
        compact.setOnCheckedChangeListener((button, checked) -> getSharedPreferences("reader", MODE_PRIVATE)
                .edit().putBoolean("compactSpeech", checked).apply());
        root.addView(compact);
        setContentView(root);
    }
}
