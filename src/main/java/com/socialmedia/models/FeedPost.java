package com.socialmedia.models;

import java.sql.Timestamp;

public class FeedPost {

    private final int postId;
    private final int userId;
    private final String userName;
    private final String content;
    private final String img;
    private final Timestamp createdAt;

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