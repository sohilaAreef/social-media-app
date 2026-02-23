package com.socialmedia.models;

import java.security.Timestamp;

public class Friend {
    private int userId;
    private int friendId;
    private String status;
    private Timestamp createdAt;

    public Friend() {
    }

    public Friend(Timestamp createdAt, int friendId, String status, int userId) {
        this.createdAt = createdAt;
        this.friendId = friendId;
        this.status = status;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
}
