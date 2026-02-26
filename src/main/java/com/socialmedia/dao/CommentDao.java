package com.socialmedia.dao;

import com.socialmedia.config.DatabaseConfig;
import com.socialmedia.models.Comment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDao {

    public void addComment(int userId, int postId, String content) throws SQLException {
        String sql = "INSERT INTO `comment` (content, post_id, user_id) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, content);
            ps.setInt(2, postId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public int countComments(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `comment` WHERE post_id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Comment> getLatestComments(int postId, int limit) throws SQLException {
        String sql = """
            SELECT id, post_id, user_id, content, created_at
            FROM `comment`
            WHERE post_id = ?
            ORDER BY created_at DESC
            LIMIT ?;
            """;

        List<Comment> list = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, postId);
            ps.setInt(2, limit);

            ResultSet(list, ps);
        }
        return list;
    }

    private void ResultSet(List<Comment> list, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Comment c = new Comment();
                c.setId(rs.getInt("id"));
                c.setPostId(rs.getInt("post_id"));
                c.setUserId(rs.getInt("user_id"));
                c.setContent(rs.getString("content"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(c);
            }
        }
    }

    public List<Comment> getAllComments(int postId) throws SQLException {
        String sql = """
            SELECT id, post_id, user_id, content, created_at
            FROM `comment`
            WHERE post_id = ?
            ORDER BY created_at DESC;
            """;

        List<Comment> list = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, postId);
            ResultSet(list, ps);
        }
        return list;
    }
}