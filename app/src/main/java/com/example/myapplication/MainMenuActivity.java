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
        // 1. START THE MUSIC SERVICE IMMEDIATELY
        // We do this before the login check so music plays during the transition
        Intent musicIntent = new Intent(this, MusicService.class);
        startService(musicIntent);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);

        // 2. CHECK LOGIN
        FirebaseUser currentUser = refAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, GameMenuActivity.class);
            startActivity(intent);
            finish();
            return; // Stop execution here
        }

        // Initialize your buttons here (as per your existing code)
        signInButton = findViewById(R.id.sign_in_button);
        signUpButton = findViewById(R.id.sign_up_button);
        // ... rest of your code
    }
}