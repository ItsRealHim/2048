package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoresActivity extends AppCompatActivity {

    private static final String TAG = "HighScoresActivity";

    private RecyclerView rvHighScores;
    private Button btnBack;
    private Button btnSortToggle;
    private List<Player> highScoresList;
    private ScoreAdapter adapter;
    private boolean isSortByHighScore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        // Initialize RecyclerView
        rvHighScores = findViewById(R.id.rvHighScores);
        rvHighScores.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Buttons
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSortToggle = findViewById(R.id.btnSortToggle);
        btnSortToggle.setOnClickListener(v -> toggleSorting());

        // Setup Adapter
        highScoresList = new ArrayList<>();
        adapter = new ScoreAdapter(highScoresList);
        rvHighScores.setAdapter(adapter);

        // Fetch scores from Firebase Realtime Database
        fetchScores();
    }

    private void toggleSorting() {
        isSortByHighScore = !isSortByHighScore;

        if (isSortByHighScore) {
            btnSortToggle.setText("Sort: High Score");
        } else {
            btnSortToggle.setText("Sort: Games Played");
        }
        // Re-sort the list and update the view
        applySort();
    }

    private void applySort() {
        // Sorts the list based on the current toggle state
        if (isSortByHighScore) {
            // Sort by highScore in descending order
            highScoresList.sort((p1, p2) -> Integer.compare(p2.getHighScore(), p1.getHighScore()));
        } else {
            // Sort by gamesPlayed in descending order
            highScoresList.sort((p1, p2) -> Integer.compare(p2.getGamesPlayed(), p1.getGamesPlayed()));
        }

        // Notify the adapter that the data has changed
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Fetches scores from Firebase Realtime Database using the reference from FBRef.java.
     */
    private void fetchScores() {
        // Use the refPlayer from your FBRef class and order by "highScore"
        Query query = FBRef.refPlayer.orderByChild("highScore").limitToLast(100); // Fetches top 100 scores

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                highScoresList.clear(); // Clear the list before adding new data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Player player = snapshot.getValue(Player.class);
                    if (player != null) {
                        highScoresList.add(player);
                    }
                }
                // Reverse the list because Realtime DB returns ascending order
                Collections.reverse(highScoresList);
                // Apply the initial sort (by high score)
                applySort();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    // --- Inner Adapter Class ---
    private class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {
        private List<Player> players;

        public ScoreAdapter(List<Player> players) {
            this.players = players;
        }

        @NonNull
        @Override
        public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_score, parent, false);
            return new ScoreViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
            Player p = players.get(position);
            holder.tvRank.setText("#" + (position + 1));
            holder.tvUsername.setText(p.getUsername());
            holder.tvScore.setText(String.valueOf(p.getHighScore()));
            holder.tvGamesPlayed.setText("Games: " + p.getGamesPlayed());
        }

        @Override
        public int getItemCount() {
            return players.size();
        }

        class ScoreViewHolder extends RecyclerView.ViewHolder {
            TextView tvRank, tvUsername, tvScore, tvGamesPlayed;

            public ScoreViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRank = itemView.findViewById(R.id.tvRank);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvScore = itemView.findViewById(R.id.tvScore);
                tvGamesPlayed = itemView.findViewById(R.id.tvGamesPlayed);
            }
        }
    }
}
