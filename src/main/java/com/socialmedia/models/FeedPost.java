package com.socialmedia.models;

import java.sql.Timestamp;

public class FeedPost {

    private int postId;
    private int userId;
    private String userName;
    private String content;
    private String img;
    private Timestamp createdAt;

    public FeedPost(int postId, int userId, String userName,
                    String content, String img, Timestamp createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.img = img;
        this.createdAt = createdAt;
    }

    public int getPostId() {
        return postId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public String getImg() {
        return img;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}