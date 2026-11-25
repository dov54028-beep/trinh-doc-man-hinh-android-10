package com.vuong.reader;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Independent Android 10 screen reader core with defensive node handling. */
public class ScreenReaderService extends AccessibilityService {
    private static final long DOUBLE_TAP_MS = 450L;
    private static final long NAVIGATION_SCROLL_DELAY_MS = 350L;
    private static ScreenReaderService instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextToSpeech tts;
    private FeedbackSounds sounds;
    private AccessibilityNodeInfo hoveredNode;
    private long lastHoverAt;
    private CharSequence lastText;
    private long lastSpokenAt;

    @Override public void onServiceConnected() {
        if (sounds != null) sounds.release();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        instance = this;
        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.notificationTimeout = 80;
        setServiceInfo(info);
        sounds = new FeedbackSounds(this);
        tts = new TextToSpeech(this, status -> {
            TextToSpeech engine = tts;
            if (status == TextToSpeech.SUCCESS && engine != null) {
                engine.setLanguage(new Locale("vi", "VN"));
            }
        });
        playActivate();
        speak("Trình đọc màn hình đã bật");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            replaceHovered(null);
            lastText = null;
        }
        if (type == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER) {
            handleHover(event);
            return;
        }
        if (type == AccessibilityEvent.TYPE_VIEW_CLICKED || type == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            playActivate();
        }
        CharSequence text = textFor(event);
        if (text != null && text.length() > 0 && shouldSpeak(text)) speak(text.toString());
    }

    private void handleHover(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;
        try {
            long now = System.currentTimeMillis();
            boolean secondTap = hoveredNode != null && now - lastHoverAt <= DOUBLE_TAP_MS
                    && sameNode(hoveredNode, source);
            if (secondTap && isActivatable(source)) {
                if (source.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    playActivate();
                    speak("Đã kích hoạt");
                } else playError();
                lastHoverAt = 0L;
                replaceHovered(null);
                return;
            }
            replaceHovered(AccessibilityNodeInfo.obtain(source));
            lastHoverAt = now;
            source.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            playFocus();
            CharSequence text = label(source);
            if (text != null && shouldSpeak(text)) speak(text.toString());
        } finally {
            source.recycle();
        }
    }

    private boolean isActivatable(AccessibilityNodeInfo node) {
        return node.isClickable() || node.isFocusable() || node.isLongClickable()
                || node.isCheckable() || node.isEditable();
    }

    private boolean sameNode(AccessibilityNodeInfo a, AccessibilityNodeInfo b) {
        if (a.getWindowId() != b.getWindowId()) return false;
        Rect aBounds = new Rect();
        Rect bBounds = new Rect();
        a.getBoundsInScreen(aBounds);
        b.getBoundsInScreen(bBounds);
        if (!aBounds.equals(bBounds)) return false;
        CharSequence aId = a.getViewIdResourceName();
        CharSequence bId = b.getViewIdResourceName();
        if (aId != null || bId != null) return aId != null && aId.equals(bId);
        return equalText(a.getText(), b.getText()) && equalText(a.getContentDescription(), b.getContentDescription())
                && equalText(a.getClassName(), b.getClassName());
    }

    private boolean equalText(CharSequence a, CharSequence b) {
        return a == null ? b == null : a.equals(b);
    }

    private CharSequence textFor(AccessibilityEvent event) {
        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || type == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
                || type == AccessibilityEvent.TYPE_VIEW_CLICKED
                || type == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
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
        boolean duplicate = text.equals(lastText) && now - lastSpokenAt < 500L;
        if (!duplicate) { lastText = text; lastSpokenAt = now; }
        return !duplicate;
    }

    @Override public boolean onGesture(int gestureId) {
        if (gestureId == GESTURE_SWIPE_LEFT) {
            playSwipe();
            moveFocus(View.FOCUS_BACKWARD);
            return true;
        }
        if (gestureId == GESTURE_SWIPE_RIGHT) {
            playSwipe();
            moveFocus(View.FOCUS_FORWARD);
            return true;
        }
        if (gestureId == GESTURE_SWIPE_UP) {
            playSwipe();
            scroll(true);
            return true;
        }
        if (gestureId == GESTURE_SWIPE_DOWN) {
            playSwipe();
            scroll(false);
            return true;
        }
        return super.onGesture(gestureId);
    }

    private void moveFocus(int direction) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { playError(); return; }
        AccessibilityNodeInfo current = null;
        List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        try {
            current = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
            collectReadableNodes(root, nodes, true, new HashSet<String>());
            Collections.sort(nodes, SCREEN_ORDER);
            AccessibilityNodeInfo candidate = findOrderedCandidate(nodes, current, direction);
            if (candidate == null) candidate = focusSearchFallback(root, current, direction);
            if (candidate != null && focusAndSpeak(candidate)) {
                // Candidate is recycled below after the action is complete.
            } else if (!nodes.isEmpty()) {
                scrollForMore(root, current, direction);
            } else {
                playError();
                speak(direction == View.FOCUS_FORWARD ? "Không có mục tiếp theo" : "Không có mục trước");
            }
            if (candidate != null) {
                boolean ownedByList = false;
                for (AccessibilityNodeInfo node : nodes) if (node == candidate) { ownedByList = true; break; }
                if (ownedByList) nodes.remove(candidate);
                candidate.recycle();
            }
        } finally {
            for (AccessibilityNodeInfo node : nodes) node.recycle();
            if (current != null) current.recycle();
            root.recycle();
        }
    }

    private boolean focusAndSpeak(AccessibilityNodeInfo candidate) {
        if (!candidate.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)) return false;
        playFocus();
        CharSequence candidateLabel = label(candidate);
        if (candidateLabel != null) speak(candidateLabel.toString());
        return true;
    }

    private void collectReadableNodes(AccessibilityNodeInfo node, List<AccessibilityNodeInfo> result,
                                      boolean isRoot, Set<String> seen) {
        if (node == null) return;
        if (!isRoot && node.isVisibleToUser()) {
            CharSequence nodeLabel = label(node);
            String key = nodeKey(node, nodeLabel);
            if (nodeLabel != null && seen.add(key)) result.add(AccessibilityNodeInfo.obtain(node));
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectReadableNodes(child, result, false, seen);
                child.recycle();
            }
        }
    }

    private String nodeKey(AccessibilityNodeInfo node, CharSequence nodeLabel) {
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        return node.getWindowId() + ":" + bounds.toShortString() + ":" + String.valueOf(nodeLabel);
    }

    private static final Comparator<AccessibilityNodeInfo> SCREEN_ORDER = (a, b) -> {
        Rect ra = new Rect();
        Rect rb = new Rect();
        a.getBoundsInScreen(ra);
        b.getBoundsInScreen(rb);
        if (ra.top != rb.top) return Integer.compare(ra.top, rb.top);
        if (ra.left != rb.left) return Integer.compare(ra.left, rb.left);
        return Integer.compare(ra.bottom, rb.bottom);
    };

    private AccessibilityNodeInfo findOrderedCandidate(List<AccessibilityNodeInfo> nodes,
                                                        AccessibilityNodeInfo current, int direction) {
        if (nodes.isEmpty()) return null;
        int currentIndex = -1;
        for (int i = 0; i < nodes.size(); i++) {
            if (current != null && sameNode(nodes.get(i), current)) { currentIndex = i; break; }
        }
        int nextIndex = direction == View.FOCUS_FORWARD ? currentIndex + 1 : currentIndex - 1;
        if (currentIndex < 0) nextIndex = direction == View.FOCUS_FORWARD ? 0 : nodes.size() - 1;
        return nextIndex >= 0 && nextIndex < nodes.size() ? nodes.get(nextIndex) : null;
    }

    private AccessibilityNodeInfo focusSearchFallback(AccessibilityNodeInfo root,
                                                       AccessibilityNodeInfo current, int direction) {
        AccessibilityNodeInfo base = current == null ? root : current;
        AccessibilityNodeInfo result = base.focusSearch(direction);
        if (result == null) return null;
        AccessibilityNodeInfo copy = AccessibilityNodeInfo.obtain(result);
        result.recycle();
        return copy;
    }

    private void scrollForMore(AccessibilityNodeInfo root, AccessibilityNodeInfo current, int direction) {
        AccessibilityNodeInfo start = current == null ? root : current;
        AccessibilityNodeInfo scrollable = findScrollableAncestor(start);
        boolean forward = direction == View.FOCUS_FORWARD;
        boolean scrolled = scrollable != null && scrollable.performAction(forward
                ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        if (scrollable != null) scrollable.recycle();
        if (scrolled) {
            playSwipe();
            handler.postDelayed(() -> moveFocus(direction), NAVIGATION_SCROLL_DELAY_MS);
        } else {
            playError();
            speak(forward ? "Đã tới mục cuối" : "Đã tới mục đầu");
        }
    }

    private AccessibilityNodeInfo findScrollableAncestor(AccessibilityNodeInfo start) {
        AccessibilityNodeInfo cursor = AccessibilityNodeInfo.obtain(start);
        while (cursor != null) {
            if (cursor.isScrollable()) return cursor;
            AccessibilityNodeInfo parent = cursor.getParent();
            cursor.recycle();
            cursor = parent;
        }
        return null;
    }

    private void scroll(boolean forward) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { playError(); return; }
        AccessibilityNodeInfo focus = null;
        AccessibilityNodeInfo scrollable = null;
        try {
            focus = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
            scrollable = findScrollableAncestor(focus == null ? root : focus);
            boolean ok = scrollable != null && scrollable.performAction(forward
                    ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            if (ok) speak(forward ? "Cuộn xuống" : "Cuộn lên");
            else { playError(); speak("Không có vùng cuộn"); }
        } finally {
            if (scrollable != null) scrollable.recycle();
            if (focus != null) focus.recycle();
            root.recycle();
        }
    }

    private CharSequence label(AccessibilityNodeInfo node) {
        CharSequence result = node.getContentDescription();
        if (result == null || result.length() == 0) result = node.getText();
        if (result == null || result.length() == 0) {
            String cls = node.getClassName() == null ? "" : node.getClassName().toString();
            if (cls.contains("Button")) result = "Nút";
            else if (cls.contains("EditText")) result = "Ô nhập liệu";
            else if (node.isCheckable()) result = "Mục chọn";
        }
        if (result == null || result.length() == 0) return null;
        StringBuilder output = new StringBuilder(result.toString());
        if (node.isChecked()) output.append(", đã chọn");
        if (!node.isEnabled()) output.append(", bị vô hiệu hóa");
        return output.toString();
    }

    private void replaceHovered(AccessibilityNodeInfo replacement) {
        if (hoveredNode != null) hoveredNode.recycle();
        hoveredNode = replacement;
    }

    private void playFocus() { if (sounds != null) sounds.focus(); }
    private void playSwipe() { if (sounds != null) sounds.swipe(); }
    private void playActivate() { if (sounds != null) sounds.activate(); }
    private void playError() { if (sounds != null) sounds.error(); }

    @Override public void onInterrupt() { playError(); speak("Đã tạm dừng"); }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        replaceHovered(null);
        if (sounds != null) { sounds.release(); sounds = null; }
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        lastText = null;
        instance = null;
        super.onDestroy();
    }

    private void speak(String value) {
        if (tts == null || value == null || value.length() == 0) return;
        String spoken = value;
        if (getSharedPreferences("reader", MODE_PRIVATE).getBoolean("compactSpeech", false)) {
            int comma = value.indexOf(',');
            if (comma > 0) spoken = value.substring(0, comma);
        }
        tts.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "reader");
    }

    public static boolean isRunning() { return instance != null; }
    public static void speakFromTest(String value) { if (instance != null) instance.speak(value); }
    public static void repeatCurrent() {
        if (instance != null && instance.lastText != null && instance.lastText.length() > 0)
            instance.speak(instance.lastText.toString());
    }
}
