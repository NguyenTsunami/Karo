package com.example.karo.model;

public class RoomDetail {
    private String roomDocument;
    private Room room;
    private User userRoleX;
    private User userRoleO;

    public RoomDetail() {
    }

    public RoomDetail(String roomDocument, Room room, User userRoleX, User userRoleO) {
        this.roomDocument = roomDocument;
        this.room = room;
        this.userRoleX = userRoleX;
        this.userRoleO = userRoleO;
    }

    public User getUserRoleX() {
        return userRoleX;
    }

    public void setUserRoleX(User userRoleX) {
        this.userRoleX = userRoleX;
    }

    public User getUserRoleO() {
        return userRoleO;
    }

    public void setUserRoleO(User userRoleO) {
        this.userRoleO = userRoleO;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getRoomDocument() {
        return roomDocument;
    }

    public void setRoomDocument(String roomDocument) {
        this.roomDocument = roomDocument;
    }

    public void copy(RoomDetail roomDetail) {
        this.setRoomDocument(roomDetail.getRoomDocument());
        this.setRoom(roomDetail.getRoom());
        this.setUserRoleX(roomDetail.getUserRoleX());
        this.setUserRoleO(roomDetail.getUserRoleO());
    }
}
