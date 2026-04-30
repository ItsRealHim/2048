package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MusicService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private float currentVolume = 1.0f;
    private final IBinder binder = new MusicBinder();

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (requestAudioFocus()) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        abandonAudioFocus();
    }

    // ----------------------
    // MediaPlayer helpers
    // ----------------------

    private void initMediaPlayer() {
        releaseMediaPlayer(); // safety

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(currentVolume, currentVolume);
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // ----------------------
    // Public API
    // ----------------------

    public void setVolume(float volume) {
        volume = (float) Math.sqrt(volume);
        currentVolume = volume;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    public float getVolume() {
        return currentVolume * currentVolume;
    }

    // ----------------------
    // Audio Focus Handling
    // ----------------------

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) {
                    initMediaPlayer();
                }

                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }

                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(currentVolume, currentVolume);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // permanent loss → pause (not stop/release)
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    float duckVolume = currentVolume * 0.2f;
                    mediaPlayer.setVolume(duckVolume, duckVolume);
                }
                break;
        }
    }

    // ----------------------
    // Audio Focus Requests
    // ----------------------

    private boolean requestAudioFocus() {
        if (audioManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();

            return audioManager.requestAudioFocus(audioFocusRequest)
                    == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        } else {
            return audioManager.requestAudioFocus(
                    this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void abandonAudioFocus() {
        if (audioManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }
}