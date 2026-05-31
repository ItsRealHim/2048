package com.example.myapplication;


import java.util.HashMap;
import java.util.Map;

public class Player {
    private String username;
    private int highScore;
    private int gamesPlayed;
    private String PlayerID;

    public Player() {
        this("");
    }

    public Player(String username) {
        this.username = username;
        this.highScore = 0;
        this.gamesPlayed = 0;
        this.PlayerID = "0";
    }


    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    public String getPlayerID() {
        return PlayerID;
    }

    public void setPlayerID(String playerID) {
        PlayerID = playerID;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
