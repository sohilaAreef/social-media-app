package com.socialmedia.dao;

import com.socialmedia.config.DatabaseConfig;
import com.socialmedia.models.Profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;


public class ProfileDao {


    public Profile getByUserId(int userId) throws SQLException {
        String sql = "SELECT user_id, img, bio FROM `profile` WHERE user_id = ? LIMIT 1";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new Profile(userId, null, "");
                return new Profile(
                        rs.getInt("user_id"),
                        rs.getString("img"),
                        rs.getString("bio")
                );
            }
        }
    }

    public void update(int userId, String img, String bio) throws SQLException {
        String sql = "UPDATE `profile` SET img = ?, bio = ? WHERE user_id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, img);
            ps.setString(2, bio);
            ps.setInt(3, userId);

            ps.executeUpdate();
        }
    }

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

    public record ProfileData(int userId, String img, String bio) {
            public ProfileData(int userId, String img, String bio) {
                this.userId = userId;
                this.img = img;
                this.bio = bio == null ? "" : bio;
            }
        }
}