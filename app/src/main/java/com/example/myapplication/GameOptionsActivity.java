package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameOptionsActivity extends AppCompatActivity {

    private TextView tvVolumePercentage;
    private SeekBar sbVolume;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_options);

        tvVolumePercentage = findViewById(R.id.tvVolumePercentage);
        sbVolume = findViewById(R.id.sbVolume);
        btnBack = findViewById(R.id.btnBackToMenu);

        // 1. Get current volume from Service
        if (MusicService.instance != null) {
            float currentVol = MusicService.instance.getVolume();

            // To set the slider correctly, we reverse the math: sqrt(volume)
            // This ensures the slider handle matches the curved volume level
            int progress = (int) (Math.sqrt(currentVol) * 100);

            sbVolume.setProgress(progress);
            tvVolumePercentage.setText(progress + "%");
        }

        // 2. Slider Logic with Exponential Curve
        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && MusicService.instance != null) {
                    // (progress / 100)^2 makes the volume decrease MUCH faster.
                    // At 50% slider, actual volume is 25%. At 20% slider, it's 4%.
                    float volumeLevel = (float) Math.pow(progress / 100f, 2);
                    MusicService.instance.setVolume(volumeLevel);
                }
                tvVolumePercentage.setText(progress + "%");
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 3. Back Button
        btnBack.setOnClickListener(v -> {
            // We only call finish().
            // DO NOT call stopService here, or the music will die.
            finish();
        });
    }
}