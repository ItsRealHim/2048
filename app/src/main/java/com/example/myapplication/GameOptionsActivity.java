package com.example.myapplication;

import static com.example.myapplication.FBRef.refAuth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameOptionsActivity extends AppCompatActivity implements View.OnClickListener{

    private MusicService musicService;
    private boolean isBound = false;
    private TextView tvVolumePercentage;
    private SeekBar seekBarVolume;
    private TextView btnBackToMenu;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            // sync slider with current volume
            float currentVolume = musicService.getVolume();
            setVisualVolume(currentVolume);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            musicService = null;
        }
    };

    private void setVisualVolume(float volume) {
        int percentage = (int) (volume * 100);
        seekBarVolume.setProgress(percentage);
        tvVolumePercentage.setText(String.format("%d%%", percentage));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_options);

        seekBarVolume = findViewById(R.id.sbVolume);
        tvVolumePercentage = findViewById(R.id.tvVolumePercentage);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnBackToMenu.setOnClickListener(this);

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null) {
                    float volume = progress / 100f;
                    setVisualVolume(volume);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnBackToMenu) {
            refAuth.signOut();
            Intent intent = new Intent(this, GameMenuActivity.class);
            startActivity(intent);
            finish();
        }
    }
}