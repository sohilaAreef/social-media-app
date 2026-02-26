package com.socialmedia.services;

import com.socialmedia.dao.ProfileDao;
import com.socialmedia.dao.UserDao;
import com.socialmedia.models.User;
import com.socialmedia.utils.PasswordHasher;
import com.socialmedia.utils.Session;

public class AuthService {

    private static final UserDao userDAO = new UserDao();
    private final ProfileDao profileDAO = new ProfileDao();

    public User register(String name, String email, String plainPassword) {
        validateRegister(name, email, plainPassword);

        if (userDAO.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String hashed = PasswordHasher.hash(plainPassword);
        User user = userDAO.create(name, email, hashed);

        // create profile row automatically
        profileDAO.createDefaultProfile(user.getId());

        return user;
    }

    public static User login(String email, String plainPassword) {
        if (email == null || email.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        User user = userDAO.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("Invalid credentials");

        if (!PasswordHasher.verify(plainPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return user;
    }

    private void validateRegister(String name, String email, String password) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (!email.contains("@")) throw new IllegalArgumentException("Email is not valid");
        if (password == null || password.length() < 6) throw new IllegalArgumentException("Password must be at least 6 chars");
    }
    public void logout() {
        Session.clear();
    }
}
