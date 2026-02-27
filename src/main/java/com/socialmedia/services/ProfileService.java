package com.socialmedia.services;

import com.socialmedia.dao.ProfileDao;
import com.socialmedia.dao.ProfileDao.ProfileData;
import com.socialmedia.models.Profile;

import java.sql.SQLException;

public class ProfileService {

    private final ProfileDao profileDao = new ProfileDao();

    public Profile loadProfile(int userId) throws SQLException {
        return profileDao.getByUserId(userId);
    }

    public void updateProfile(int userId, String imgPath, String bio) throws SQLException {
        profileDao.update(userId, imgPath, bio);
    }
}