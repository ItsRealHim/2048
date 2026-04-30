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
    private boolean wasPlayingBeforeLoss = false;
    private boolean isPrepared = false;

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
            if (mediaPlayer != null && isPrepared && !mediaPlayer.isPlaying()) {
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
    // MediaPlayer
    // ----------------------

    private void initMediaPlayer() {
        releaseMediaPlayer();

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(currentVolume, currentVolume);
            isPrepared = true;
        } else {
            isPrepared = false;
        }
    }

    private void releaseMediaPlayer() {
        isPrepared = false;

        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception ignored) {}

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // ----------------------
    // Public API
    // ----------------------

    public void setVolume(float volume) {
        volume = volume * volume; // perceived curve
        currentVolume = volume;

        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    public float getVolume() {
        return (float) Math.sqrt(currentVolume);
    }

    // ----------------------
    // Audio Focus
    // ----------------------

    @Override
    public void onAudioFocusChange(int focusChange) {

        if (mediaPlayer == null) return;

        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_GAIN:
                mediaPlayer.setVolume(currentVolume, currentVolume);

                if (wasPlayingBeforeLoss && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }

                wasPlayingBeforeLoss = false;
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                wasPlayingBeforeLoss = mediaPlayer.isPlaying();

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mediaPlayer.setVolume(
                        currentVolume * 0.2f,
                        currentVolume * 0.2f
                );
                break;
        }
    }

    // ----------------------
    // Audio Focus Request
    // ----------------------

    private boolean requestAudioFocus() {
        if (audioManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attrs)
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