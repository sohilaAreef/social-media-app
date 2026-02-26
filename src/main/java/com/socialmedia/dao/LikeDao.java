package com.socialmedia.dao;

import com.socialmedia.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LikeDao {

    public boolean isLiked(int userId, int postId) throws SQLException {
        String sql = "SELECT 1 FROM `likes` WHERE user_id = ? AND post_id = ? LIMIT 1";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, postId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void addLike(int userId, int postId) throws SQLException {
        String sql = "INSERT INTO `likes` (user_id, post_id) VALUES (?, ?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, postId);
            ps.executeUpdate();
        }
    }

    public void removeLike(int userId, int postId) throws SQLException {
        String sql = "DELETE FROM `likes` WHERE user_id = ? AND post_id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, postId);
            ps.executeUpdate();
        }
    }

    public int countLikes(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `likes` WHERE post_id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}