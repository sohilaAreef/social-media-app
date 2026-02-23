package com.socialmedia.utils;

public final class Session {
    private static String currentUserEmail;

    private Session() {}

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static void setCurrentUserEmail(String email) {
        currentUserEmail = email;
    }

    public static void clear() {
        currentUserEmail = null;
    }
}