package com.example.myapplication;

import static com.example.myapplication.FBRef.refAuth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private CardView playGame, cvHighScores;
    private CardView cvGameOptions; // 1. Declare the CardView with the correct ID name
    private Button btnProfileDetails;
    private TextView signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Find all the views by their ID
        signOut = findViewById(R.id.signOut);
        playGame = findViewById(R.id.playgame);
        cvHighScores = findViewById(R.id.cvHighScores);
        btnProfileDetails = findViewById(R.id.btnProfileDetails);
        cvGameOptions = findViewById(R.id.cvGameOptions); // Find the game options CardView

        // 3. Set the click listener for all interactive views
        signOut.setOnClickListener(this);
        playGame.setOnClickListener(this);
        cvHighScores.setOnClickListener(this);
        btnProfileDetails.setOnClickListener(this);
        cvGameOptions.setOnClickListener(this); // Set the listener for the options CardView
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.signOut) {
            refAuth.signOut();
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        } else if (viewId == R.id.cvHighScores) {
            Intent intent = new Intent(this, HighScoresActivity.class);
            startActivity(intent);
        } else if (viewId == R.id.playgame) {
            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
        } else if (viewId == R.id.cvGameOptions) { // 4. Handle the click for the options button
            Intent intent = new Intent(this, GameOptionsActivity.class);
            startActivity(intent);
        }
    }
}
