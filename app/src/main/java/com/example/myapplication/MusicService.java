package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder; // Import Binder
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class MusicService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean playbackDelayed = false;
    private final IBinder binder = new MusicBinder(); // Create a binder instance

    // Inner class that clients can use to interact with the service
    public class MusicBinder extends Binder {
        public MusicService getService() {
            // Return this instance of MusicService so clients can call public methods
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Return the binder instance for clients
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(1.0f, 1.0f);
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        abandonAudioFocus();
    }

    // --- Public method for clients to call ---
    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        // Check for null media player to prevent crashes
        if (mediaPlayer == null && focusChange != AudioManager.AUDIOFOCUS_GAIN) {
            // If we've lost focus and the player is already released, do nothing.
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                abandonAudioFocus();
            }
            return;
        }

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer == null) {
                    // Re-initialize if it was released
                    onCreate();
                    if(requestAudioFocus()) mediaPlayer.start();
                } else if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                playbackDelayed = false;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                abandonAudioFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playbackDelayed = true;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    private boolean requestAudioFocus() {
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

            return audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioManager != null && audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }
        } else {
            if (audioManager != null) {
                audioManager.abandonAudioFocus(this);
            }
        }
    }
}
