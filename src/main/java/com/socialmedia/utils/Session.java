package com.socialmedia.utils;

import com.socialmedia.models.User;

public class Session {
    private static User currentUser;
    private static Integer viewedUserId;

    public static Integer getViewedUserId() { return viewedUserId; }
    public static void setViewedUserId(Integer id) { viewedUserId = id; }
    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }
    public static void clear() { currentUser = null; }
}