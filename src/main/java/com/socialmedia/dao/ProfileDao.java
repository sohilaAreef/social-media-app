package com.socialmedia.dao;

import com.socialmedia.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileDao {

    public void createDefaultProfile(int userId) {
        String sql = "INSERT INTO `profile` (user_id, img, bio) VALUES (?,?,?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, "images/default-avatar.png");
            ps.setString(3, "");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}