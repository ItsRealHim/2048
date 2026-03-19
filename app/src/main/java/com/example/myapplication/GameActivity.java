package com.example.myapplication;

import static com.example.myapplication.FBRef.refAuth;
import static com.example.myapplication.FBRef.refPlayer;

import android.media.AudioAttributes;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.example.myapplication.GameModel.Direction;
import com.example.myapplication.GameModel.TileChange;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    private boolean hasShownGameOver = false;
    private TextToSpeech tts;
    // --- UI Components ---
    private GridView gameGrid;
    private TextView tvScore;
    private TextView tvHighScore;
    private Button btnNewGame;

    // --- Logic and State Management ---
    private GameModel gameModel; // Use the GameModel for all game logic
    private TileAdapter tileAdapter;
    private GestureDetectorCompat gestureDetector;

    // --- High Score Persistence ---
    private int highScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize the model first, as it holds the state
        gameModel = new GameModel();

        // Initialize UI components
        gameGrid = findViewById(R.id.gameGrid);
        tvScore = findViewById(R.id.tvScore);
        tvHighScore = findViewById(R.id.tvHighScore);
        btnNewGame = findViewById(R.id.btnNewGame);

        // Load the saved high score
        FirebaseUser user = refAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            refPlayer.child(uid).child("highScore").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    highScore = task.getResult().getValue(Integer.class);
                    tvHighScore.setText(String.valueOf(highScore));
                }
            });
        }
        // Setup the adapter with the board from our model
        tileAdapter = new TileAdapter(this, gameModel.getBoard());
        gameGrid.setAdapter(tileAdapter);

        // Setup the gesture detector for swipe handling
        setupGestureDetector();

        // Start a new game using the model
        startNewGame();

        // Set listeners
        btnNewGame.setOnClickListener(v -> startNewGame());

        // The touch listener now only detects gestures and passes them to the handler
        gameGrid.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true; // Consume the event to prevent other touch actions
        });

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        tts.setAudioAttributes(audioAttributes);
    }

    private void incrementGameCount() {
        // Load the saved games played
        FirebaseUser user = refAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        String uid = user.getUid();
        refPlayer.child(uid).child("gamesPlayed").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int gamesPlayed = task.getResult().getValue(Integer.class);
                gamesPlayed++;
                refPlayer.child(uid).child("gamesPlayed").setValue(gamesPlayed);
            }
        });

    }

    /**
     * Resets the game by calling the model's startNewGame method and updating the UI.
     */
    private void startNewGame() {
        gameModel.startNewGame(); // All reset logic is now in the model
        updateScore();
        tileAdapter.notifyDataSetChanged(); // Refresh the grid display
        hasShownGameOver = false;
    }

    /**
     * Handles the swipe gesture, delegates logic to the model, and updates the UI if needed.
     *
     * @param direction The direction of the swipe.
     */
    private void handleSwipe(Direction direction) {
        if (hasShownGameOver)
            return;
        // The model now tells us if a move was successful
        List<TileChange> changes = gameModel.handleSwipe(direction);
        for (TileChange change : changes) {
            Log.d("GameActivity", "TileChange: " + change);
        }

        if (!changes.isEmpty()) {
            // If the board changed, update the UI
            updateScore();
            tileAdapter.notifyDataSetChanged();
        }

        // Check for game over
        if (gameModel.isGameOver()) {
            hasShownGameOver = true;
            if (tts != null) {
                tts.speak("Game Over", TextToSpeech.QUEUE_FLUSH, null, "game_over_id");
            }
            Snackbar.make(findViewById(android.R.id.content), "Game Over!", Snackbar.LENGTH_SHORT).show();
            incrementGameCount();
        }
    }

    /**
     * Updates the score and high score display from the model's state.
     */
    private void updateScore() {
        int currentScore = gameModel.getScore();
        tvScore.setText(String.valueOf(currentScore));

        if (currentScore > highScore) {
            highScore = currentScore;
            tvHighScore.setText(String.valueOf(highScore));

            // Save the new high score to Firebase using FBRef
            FirebaseUser user = refAuth.getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                refPlayer.child(uid).child("highScore").setValue(highScore);
            }
        } else {
            tvHighScore.setText(String.valueOf(highScore));
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Sets up the GestureDetector to listen for fling gestures (swipes).
     * This code remains unchanged as it's part of the View/Controller layer.
     */
    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(
                    MotionEvent e1,
                    MotionEvent e2,
                    float velocityX,
                    float velocityY
            ) {
                try {
                    if (e1 == null || e2 == null) return false;

                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();

                    if (Math.abs(diffX) > Math.abs(diffY)) {

                        // Horizontal swipe
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                handleSwipe(Direction.RIGHT);
                            } else {
                                handleSwipe(Direction.LEFT);
                            }
                            return true;
                        }
                    } else {

                        // Vertical swipe
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                handleSwipe(Direction.DOWN);
                            } else {
                                handleSwipe(Direction.UP);
                            }
                            return true;
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return false;
            }
        });
    }
}
