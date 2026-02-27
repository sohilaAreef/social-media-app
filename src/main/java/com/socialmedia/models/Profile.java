package com.socialmedia.models;

import java.sql.Timestamp;

public class Profile {
    private int userId;
    private String img;
    private String bio;
    private Timestamp createdAt;

    public Profile() {
    }

    public Profile(int userId, String bio, String img, Timestamp createdAt) {
        this.bio = bio;
        this.createdAt = createdAt;
        this.img = img;
        this.userId = userId;
    }

    public Profile(int userId, String bio, String img) {
        this.bio = bio;
        this.img = img;
        this.userId = userId;
    }
    
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
}
