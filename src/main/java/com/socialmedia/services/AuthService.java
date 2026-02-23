package com.socialmedia.services;

import com.socialmedia.utils.Session;

public class AuthService {

    // مؤقت: لحد ما نربط MySQL
    private static final String DEMO_EMAIL = "test@test.com";
    private static final String DEMO_PASSWORD = "1234";

    public boolean login(String email, String password) {
        if (email == null || password == null) return false;

        boolean ok = DEMO_EMAIL.equalsIgnoreCase(email.trim()) && DEMO_PASSWORD.equals(password);
        if (ok) {
            Session.setCurrentUserEmail(email.trim());
        }
        return ok;
    }

    public void logout() {
        Session.clear();
    }
}