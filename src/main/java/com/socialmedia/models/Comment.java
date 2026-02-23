package com.socialmedia.models;

import java.security.Timestamp;

public class Comment {
    private int id;
    private int postId;
    private int userId;
    private String content;
    private Timestamp createdAt;

    public Comment() {
    }

    public Comment(String content, Timestamp createdAt, int id, int postId, int userId) {
        this.content = content;
        this.createdAt = createdAt;
        this.id = id;
        this.postId = postId;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
}
