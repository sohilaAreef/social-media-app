package com.socialmedia.models;

import java.sql.Timestamp;

public class FeedPost {
    private int postId;
    private int userId;
    private String userName;
    private String content;
    private String img;
    private String privacy;
    private Timestamp createdAt;

    // ✅ FIXED constructor
    public FeedPost(int postId, int userId, String userName, String content, String img, Timestamp createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.img = img;
        this.createdAt = createdAt;
    }

    // لو عايز كمان constructor شامل privacy
    public FeedPost(int postId, int userId, String userName, String content, String img, String privacy, Timestamp createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.img = img;
        this.privacy = privacy;
        this.createdAt = createdAt;
    }

    public FeedPost() {}

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public String getPrivacy() { return privacy; }
    public void setPrivacy(String privacy) { this.privacy = privacy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}