package com.socialmedia.models;

import java.sql.Timestamp;

public class UserProfile {
    //  DTO
    private int id;
    private String name;
    private String email;
    private String img;
    private String bio;
    private Timestamp createdAt;

    public UserProfile(int id, String name, String email,
                       String img, String bio, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.img = img;
        this.bio = bio;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getImg() { return img; }
    public String getBio() { return bio; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name;}
    public void setImg(String img) { this.img = img; }
    public void setBio(String bio) { this.bio = bio; }
}