package com.example.myapplication;


import java.util.HashMap;
import java.util.Map;

public class Player {
    private String username;
    private int highScore;
    private int gamesPlayed;
    private String PlayerID;

    public Player() {
        // Essential for Firebase to map data to this class
    }

    public Player(String username) {
        this.username = username;
        this.highScore = 0;
        this.gamesPlayed = 0;
        this.PlayerID = "0";
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("highScore", highScore);
        map.put("gamesPlayed", gamesPlayed);
        map.put("PlayerID", PlayerID);
        return map;
    }

    public static Player mapToPlayer(Map<String, Object> map) {
        Player p = new Player();
        p.setUsername((String) map.get("username"));
        p.setHighScore((Integer) map.get("highScore"));
        p.setGamesPlayed((Integer) map.get("gamesPlayed"));
        p.setPlayerID((String) map.get("PlayerID"));
        return p;
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
