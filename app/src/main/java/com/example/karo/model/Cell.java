package com.example.karo.model;

public class Cell {
    private String token;
    private boolean isOnWinLine;

    public Cell() {
    }

    public Cell(String token) {
        this.token = token;
        this.isOnWinLine = false;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isOnWinLine() {
        return isOnWinLine;
    }

    public void setOnWinLine(boolean onWinLine) {
        isOnWinLine = onWinLine;
    }
}
