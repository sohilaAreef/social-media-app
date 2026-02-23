package com.socialmedia.models;

import java.security.Timestamp;

public class Like {
    private int userId;
    private int postId;
    private Timestamp createdAt;

    public Like(Timestamp createdAt, int postId, int userId) {
        this.createdAt = createdAt;
        this.postId = postId;
        this.userId = userId;
    }

    public Like() {
    }

    public int getUserId() {
        return userId;
    }

    public int getPostId() {
        return postId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
}
