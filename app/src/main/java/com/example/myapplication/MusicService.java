package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

public class MusicService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private ExoPlayer player;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;

    private float currentVolume = 1.0f;
    private boolean wasPlayingBeforeLoss = false;

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
        initPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (requestAudioFocus()) {
            if (player != null) {
                player.setPlayWhenReady(true);
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
        abandonAudioFocus();
    }

    // ----------------------
    // ExoPlayer setup
    // ----------------------

    private void initPlayer() {

        releasePlayer();

        player = new ExoPlayer.Builder(this).build();

        MediaItem item = MediaItem.fromUri("android.resource://" + getPackageName() + "/" + R.raw.background_music);
        player.setMediaItem(item);

        player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);

        player.prepare();
        player.setVolume(currentVolume);
    }

    private void releasePlayer() {

        if (player != null) {
            player.release();
            player = null;
        }
    }

    // ----------------------
    // Public API
    // ----------------------

    public void setVolume(float volume) {
        volume = Math.max(0f, Math.min(volume, 1f));
        currentVolume = volume;

        if (player != null) {
            player.setVolume(volume);
        }
    }

    public float getVolume() {
        return currentVolume;
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void play() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    public void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    // ----------------------
    // Audio Focus
    // ----------------------

    @Override
    public void onAudioFocusChange(int focusChange) {

        if (player == null) return;

        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_GAIN:
                player.setVolume(currentVolume);

                if (wasPlayingBeforeLoss) {
                    player.setPlayWhenReady(true);
                }

                wasPlayingBeforeLoss = false;
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                wasPlayingBeforeLoss = player.isPlaying();
                player.setPlayWhenReady(false);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                player.setVolume(currentVolume * 0.2f);
                break;
        }
    }

    // ----------------------
    // Audio Focus request
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
                    .setOnAudioFocusChangeListener(this)
                    .setAcceptsDelayedFocusGain(true)
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