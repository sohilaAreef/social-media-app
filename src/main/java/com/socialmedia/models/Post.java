package com.socialmedia.models;

import java.security.Timestamp;

public class Post {
    private int id;
    private int userId;
    private String content;
    private String img;
    private Timestamp createdAt;

    public Post() {
    }

    public Post(String content, Timestamp createdAt, int id, String img, int userId) {
        this.content = content;
        this.createdAt = createdAt;
        this.id = id;
        this.img = img;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
}
