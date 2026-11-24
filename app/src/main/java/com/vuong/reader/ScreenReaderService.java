package com.vuong.reader;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;

/** Independent Android 10 screen reader core for the 0.2 milestone. */
public class ScreenReaderService extends AccessibilityService {
    private static final long DOUBLE_TAP_MS = 450L;
    private static ScreenReaderService instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextToSpeech tts;
    private FeedbackSounds sounds;
    private AccessibilityNodeInfo hoveredNode;
    private long lastHoverAt;
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
        info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.notificationTimeout = 60;
        setServiceInfo(info);
        sounds = new FeedbackSounds(this);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("vi", "VN"));
        });
        sounds.activate();
        speak("Trình đọc màn hình phiên bản 0.2 đã bật");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER) {
            handleHover(event);
            return;
        }
        if (type == AccessibilityEvent.TYPE_VIEW_CLICKED || type == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            sounds.activate();
        }
        CharSequence text = textFor(event);
        if (text != null && text.length() > 0 && shouldSpeak(text)) speak(text.toString());
    }

    private void handleHover(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;
        long now = System.currentTimeMillis();
        boolean secondTap = hoveredNode != null && now - lastHoverAt <= DOUBLE_TAP_MS
                && sameNode(hoveredNode, source);
        if (secondTap) {
            if (source.isClickable() || source.isFocusable() || source.isLongClickable()) {
                if (source.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    sounds.activate();
                    speak("Đã kích hoạt");
                } else sounds.error();
            }
            lastHoverAt = 0;
            replaceHovered(null);
            return;
        }
        replaceHovered(AccessibilityNodeInfo.obtain(source));
        lastHoverAt = now;
        source.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
        sounds.focus();
        CharSequence text = label(source);
        if (text != null && shouldSpeak(text)) speak(text.toString());
    }

    private boolean sameNode(AccessibilityNodeInfo a, AccessibilityNodeInfo b) {
        if (a.getWindowId() != b.getWindowId()) return false;
        if (a.getViewIdResourceName() != null && a.getViewIdResourceName().equals(b.getViewIdResourceName())) return true;
        return a.getText() != null && a.getText().equals(b.getText()) && a.getClassName() != null
                && a.getClassName().equals(b.getClassName());
    }

    private CharSequence textFor(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
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
        if (gestureId == GESTURE_SWIPE_LEFT) {
            sounds.swipe(); moveFocus(View.FOCUS_FORWARD); return true;
        }
        if (gestureId == GESTURE_SWIPE_RIGHT) {
            sounds.swipe(); moveFocus(View.FOCUS_BACKWARD); return true;
        }
        if (gestureId == GESTURE_SWIPE_UP) { sounds.swipe(); scroll(true); return true; }
        if (gestureId == GESTURE_SWIPE_DOWN) { sounds.swipe(); scroll(false); return true; }
        return super.onGesture(gestureId);
    }

    private void moveFocus(int direction) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { sounds.error(); return; }
        AccessibilityNodeInfo current = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        AccessibilityNodeInfo base = current == null ? root : current;
        AccessibilityNodeInfo candidate = base.focusSearch(direction);
        if (candidate != null && candidate.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)) {
            sounds.focus();
            CharSequence label = label(candidate);
            if (label != null) speak(label.toString());
            candidate.recycle();
        } else sounds.error();
        if (current != null) current.recycle();
        root.recycle();
    }

    private void scroll(boolean forward) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { sounds.error(); return; }
        AccessibilityNodeInfo focus = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        AccessibilityNodeInfo node = focus == null ? root : focus;
        while (node != null && !node.isScrollable()) node = node.getParent();
        boolean ok = node != null && node.performAction(forward
                ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        if (ok) { sounds.swipe(); speak(forward ? "Cuộn xuống" : "Cuộn lên"); } else sounds.error();
        if (focus != null) focus.recycle();
        root.recycle();
    }

    private CharSequence label(AccessibilityNodeInfo node) {
        CharSequence result = node.getContentDescription();
        if (result == null || result.length() == 0) result = node.getText();
        if (result == null || result.length() == 0) {
            String cls = node.getClassName() == null ? "" : node.getClassName().toString();
            if (cls.contains("Button")) result = "Nút";
            else if (cls.contains("EditText")) result = "Ô nhập liệu";
        }
        if (result == null) return null;
        if (node.isChecked()) result += ", đã chọn";
        if (!node.isEnabled()) result += ", bị vô hiệu hóa";
        return result;
    }

    private void replaceHovered(AccessibilityNodeInfo replacement) {
        if (hoveredNode != null) hoveredNode.recycle();
        hoveredNode = replacement;
    }

    @Override public void onInterrupt() { sounds.error(); speak("Đã tạm dừng"); }

    @Override public void onDestroy() {
        replaceHovered(null);
        if (sounds != null) { sounds.release(); sounds = null; }
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
