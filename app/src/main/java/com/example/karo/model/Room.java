package com.example.karo.model;

public class Room {
    private String playerRoleXEmail;
    private String playerRoleOEmail;
    private int playerRoleXState;
    private int playerRoleOState;
    private int pickCell;

    public Room() {
    }

    public Room(String playerRoleXEmail, String playerRoleOEmail, int playerRoleXState, int playerRoleOState) {
        this.playerRoleXEmail = playerRoleXEmail;
        this.playerRoleOEmail = playerRoleOEmail;
        this.playerRoleXState = playerRoleXState;
        this.playerRoleOState = playerRoleOState;
        this.pickCell = -1;
    }

    public Room(String playerRoleXEmail, String playerRoleOEmail, int playerRoleXState, int playerRoleOState, int pickCell) {
        this.playerRoleXEmail = playerRoleXEmail;
        this.playerRoleOEmail = playerRoleOEmail;
        this.playerRoleXState = playerRoleXState;
        this.playerRoleOState = playerRoleOState;
        this.pickCell = pickCell;
    }

    public String getPlayerRoleXEmail() {
        return playerRoleXEmail;
    }

    public void setPlayerRoleXEmail(String playerRoleXEmail) {
        this.playerRoleXEmail = playerRoleXEmail;
    }

    public String getPlayerRoleOEmail() {
        return playerRoleOEmail;
    }

    public void setPlayerRoleOEmail(String playerRoleOEmail) {
        this.playerRoleOEmail = playerRoleOEmail;
    }

    public int getPlayerRoleXState() {
        return playerRoleXState;
    }

    public void setPlayerRoleXState(int playerRoleXState) {
        this.playerRoleXState = playerRoleXState;
    }

    public int getPlayerRoleOState() {
        return playerRoleOState;
    }

    public void setPlayerRoleOState(int playerRoleOState) {
        this.playerRoleOState = playerRoleOState;
    }

    public int getPickCell() {
        return pickCell;
    }

    public void setPickCell(int pickCell) {
        this.pickCell = pickCell;
    }
}
