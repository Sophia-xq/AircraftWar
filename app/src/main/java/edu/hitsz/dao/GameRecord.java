package edu.hitsz.dao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameRecord {
    private int id;
    private String playerName;
    private int score;
    private long timeMillis;           // 原始时间戳，用于排序
    private String formattedTime;      // 已格式化时间，用于显示
    private String difficulty;

    // 用于插入的构造器（不含 id，时间由系统提供）
    public GameRecord(String playerName, int score, long timeMillis, String difficulty) {
        this.id = 0;
        this.playerName = playerName;
        this.score = score;
        this.timeMillis = timeMillis;
        this.difficulty = difficulty;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        this.formattedTime = sdf.format(new Date(timeMillis));
    }

    // 用于数据库查询的完整构造器
    public GameRecord(int id, String playerName, int score, long timeMillis, String formattedTime, String difficulty) {
        this.id = id;
        this.playerName = playerName;
        this.score = score;
        this.timeMillis = timeMillis;
        this.formattedTime = formattedTime;
        this.difficulty = difficulty;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public long getTimeMillis() { return timeMillis; }
    public void setTimeMillis(long timeMillis) { this.timeMillis = timeMillis; }
    public String getFormattedTime() { return formattedTime; }
    public void setFormattedTime(String formattedTime) { this.formattedTime = formattedTime; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    @Override
    public String toString() {
        return "GameRecord{" +
                "id=" + id +
                ", playerName='" + playerName + '\'' +
                ", score=" + score +
                ", timeMillis=" + timeMillis +
                ", formattedTime='" + formattedTime + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}