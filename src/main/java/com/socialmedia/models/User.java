package com.socialmedia.models;

import java.sql.Timestamp;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private Timestamp createAt;

    public User(Timestamp createAt, String email, int id, String name, String password) {
        this.createAt = createAt;
        this.email = email;
        this.id = id;
        this.name = name;
        this.password = password;
    }
    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }
    public User(int id, String name, String email, String password, Timestamp createAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createAt = createAt;
    }

    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Timestamp getCreateAt() {
        return createAt;
    }
    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
    
}
