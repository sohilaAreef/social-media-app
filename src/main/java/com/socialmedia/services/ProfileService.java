package com.socialmedia.services;

import com.socialmedia.dao.ProfileDao;
import com.socialmedia.models.Profile;
import com.socialmedia.models.User;
import com.socialmedia.models.UserProfile;
import com.socialmedia.utils.PasswordHasher;

public class ProfileService {

    private final ProfileDao profileDao;

    public ProfileService() {
        this.profileDao = new ProfileDao();
    }
    
    public UserProfile getProfile(int userId) {
        return profileDao.getProfile(userId);
    }

    public boolean updateProfile(User user, Profile profile, String newPasswordPlain) {
        String hashedPassword = null;
        if (newPasswordPlain != null && !newPasswordPlain.isEmpty()) {
            hashedPassword = PasswordHasher.hash(newPasswordPlain);
        }
        return profileDao.updateProfile(user, profile, hashedPassword);
    }

}