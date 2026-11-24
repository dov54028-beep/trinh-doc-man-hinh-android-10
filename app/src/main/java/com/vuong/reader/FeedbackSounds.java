package com.vuong.reader;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

/** Lightweight earcons; no audio files or network access are required. */
public final class FeedbackSounds {
    private final Context context;
    private ToneGenerator tone;

    public FeedbackSounds(Context context) { this.context = context.getApplicationContext(); }

    public void focus() { play(ToneGenerator.TONE_PROP_BEEP); }
    public void swipe() { play(ToneGenerator.TONE_PROP_ACK); }
    public void activate() { play(ToneGenerator.TONE_PROP_BEEP2); }
    public void error() { play(ToneGenerator.TONE_PROP_NACK); }

    private void play(int toneType) {
        if (!context.getSharedPreferences("reader", Context.MODE_PRIVATE).getBoolean("earcons", true)) return;
        try {
            if (tone == null) tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75);
            tone.startTone(toneType, 65);
        } catch (RuntimeException ignored) { }
    }

    public void release() {
        if (tone != null) { tone.release(); tone = null; }
    }
}
