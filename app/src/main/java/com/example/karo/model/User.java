package com.example.karo.model;

import android.graphics.Bitmap;

public class User {
    private String email;
    private String password;
    private String username;
    private String avatarRef;
    private int score;
    private Bitmap avatarBitmap;
    private int rank;

    public User() {
    }

    public User(String email, String password, String username, String avatarRef, int score) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.avatarRef = avatarRef;
        this.score = score;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarRef() {
        return avatarRef;
    }

    public void setAvatarRef(String avatarRef) {
        this.avatarRef = avatarRef;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Bitmap getAvatarBitmap() {
        return avatarBitmap;
    }

    public void setAvatarBitmap(Bitmap avatarBitmap) {
        this.avatarBitmap = avatarBitmap;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
