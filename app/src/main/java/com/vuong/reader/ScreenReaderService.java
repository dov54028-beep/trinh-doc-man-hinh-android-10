package com.vuong.reader;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

/** Small, deliberately independent Android 10 reader foundation. */
public class ScreenReaderService extends AccessibilityService {
    private static ScreenReaderService instance;
    private TextToSpeech tts;
    private CharSequence lastText;
    private long lastSpokenAt;

    @Override public void onServiceConnected() {
        instance = this;
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        info.notificationTimeout = 80;
        setServiceInfo(info);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("vi", "VN"));
        });
        speak("Trình đọc màn hình đã bật");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        CharSequence text = textFor(event);
        if (text != null && text.length() > 0 && shouldSpeak(text)) speak(text.toString());
    }

    private CharSequence textFor(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getContentDescription() != null) return event.getContentDescription();
            if (event.getText() != null && !event.getText().isEmpty()) return event.getText().get(0);
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            if (event.getContentDescription() != null) return event.getContentDescription();
            if (event.getText() != null && !event.getText().isEmpty()) return join(event);
        }
        return null;
    }

    private String join(AccessibilityEvent event) {
        StringBuilder out = new StringBuilder();
        for (CharSequence part : event.getText()) {
            if (part != null && part.length() > 0) {
                if (out.length() > 0) out.append(". ");
                out.append(part);
            }
        }
        return out.toString();
    }

    private boolean shouldSpeak(CharSequence text) {
        long now = System.currentTimeMillis();
        boolean duplicate = text.equals(lastText) && now - lastSpokenAt < 500;
        if (!duplicate) { lastText = text; lastSpokenAt = now; }
        return !duplicate;
    }

    @Override public boolean onGesture(int gestureId) {
        if (gestureId == GESTURE_SWIPE_LEFT) { moveFocus(AccessibilityNodeInfo.FOCUS_FORWARD); return true; }
        if (gestureId == GESTURE_SWIPE_RIGHT) { moveFocus(AccessibilityNodeInfo.FOCUS_BACKWARD); return true; }
        return super.onGesture(gestureId);
    }

    private void moveFocus(int direction) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        AccessibilityNodeInfo next = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        if (next == null) next = root;
        AccessibilityNodeInfo candidate = next.focusSearch(direction);
        if (candidate != null) {
            candidate.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            CharSequence label = label(candidate);
            if (label != null) speak(label.toString());
            candidate.recycle();
        }
        next.recycle();
        root.recycle();
    }

    private CharSequence label(AccessibilityNodeInfo node) {
        if (node.getContentDescription() != null) return node.getContentDescription();
        if (node.getText() != null) return node.getText();
        String cls = node.getClassName() == null ? "" : node.getClassName().toString();
        if (cls.contains("Button")) return "Nút";
        return null;
    }

    @Override public void onInterrupt() { speak("Đã tạm dừng"); }

    @Override public void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        instance = null;
        super.onDestroy();
    }

    private void speak(String value) {
        if (tts != null && value != null && value.length() > 0)
            tts.speak(value, TextToSpeech.QUEUE_FLUSH, null, "reader");
    }

    public static boolean isRunning() { return instance != null; }
    public static void speakFromTest(String value) { if (instance != null) instance.speak(value); }
}
