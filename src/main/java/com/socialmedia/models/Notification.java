package com.socialmedia.models;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private int senderId;
    private int receiverId;
    private int referenceId;
    private String type;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {
    }

    public Notification(int id, int senderId, int receiverId, int referenceId, String type, boolean isRead, Timestamp createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.referenceId = referenceId;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }
    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
