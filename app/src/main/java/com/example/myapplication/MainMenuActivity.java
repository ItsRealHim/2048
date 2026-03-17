package com.example.myapplication;

import static com.example.myapplication.FBRef.refAuth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

public class MainMenuActivity extends AppCompatActivity {

    // Declare the buttons
    Button signInButton, signUpButton, playOfflineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);

        // 1. Find the buttons from the layout file
        signInButton = findViewById(R.id.sign_in_button);
        signUpButton = findViewById(R.id.sign_up_button);

        // 2. Set OnClickListener for the Sign In button
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to open the SigninActivity
                Intent intent = new Intent(MainMenuActivity.this, SigninActivity.class);
                startActivity(intent);
            }
        });

        // 3. Set OnClickListener for the Sign Up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to open the SignupActivity
                Intent intent = new Intent(MainMenuActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = refAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, GameMenuActivity.class);
            startActivity(intent);
            finish();
        }

        // Start the MusicService
        Intent musicServiceIntent = new Intent(this, MusicService.class);
        startService(musicServiceIntent);
    }
}
