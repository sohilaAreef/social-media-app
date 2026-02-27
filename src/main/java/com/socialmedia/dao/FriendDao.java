package com.socialmedia.dao;

import com.socialmedia.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendDao {

    public FriendStatusRow findRelationship(int currentUserId, int otherUserId) throws SQLException {
        String sql = """
            SELECT user_id, friend_id, status
            FROM friend
            WHERE (user_id = ? AND friend_id = ?)
               OR (user_id = ? AND friend_id = ?)
            LIMIT 1;
            """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            ps.setInt(2, otherUserId);
            ps.setInt(3, otherUserId);
            ps.setInt(4, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                int userId = rs.getInt("user_id");
                int friendId = rs.getInt("friend_id");
                String status = rs.getString("status");

                return new FriendStatusRow(userId, friendId, status);
            }
        }
    }

    public void sendRequest(int fromUserId, int toUserId) throws SQLException {
        String sql = "INSERT INTO friend (user_id, friend_id, status) VALUES (?, ?, 'PENDING')";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, fromUserId);
            ps.setInt(2, toUserId);
            ps.executeUpdate();
        }
    }

    public void cancelRequest(int fromUserId, int toUserId) throws SQLException {
        String sql = "DELETE FROM friend WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, fromUserId);
            ps.setInt(2, toUserId);
            ps.executeUpdate();
        }
    }

    public void acceptRequest(int fromUserId, int toUserId) throws SQLException {
        String sql = "UPDATE friend SET status = 'ACCEPTED' WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, fromUserId);
            ps.setInt(2, toUserId);
            ps.executeUpdate();
        }
    }

    public void declineRequest(int fromUserId, int toUserId) throws SQLException {
        String sql = "DELETE FROM friend WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, fromUserId);
            ps.setInt(2, toUserId);
            ps.executeUpdate();
        }
    }

    public void unfriend(int userA, int userB) throws SQLException {
        String sql = """
            DELETE FROM friend
            WHERE ((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?))
              AND status = 'ACCEPTED';
            """;
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userA);
            ps.setInt(2, userB);
            ps.setInt(3, userB);
            ps.setInt(4, userA);
            ps.executeUpdate();
        }
    }

    public record FriendStatusRow(int userId, int friendId, String status) { }

    public record UserMini(int id, String name) { }

    public List<UserMini> getFriends(int currentUserId) throws SQLException {
        String sql = """
        SELECT u.id, u.name
        FROM friend f
                JOIN `user` u ON u.id = IF(f.user_id = ?, f.friend_id, f.user_id)
                WHERE (f.user_id = ? OR f.friend_id = ?)
          AND f.status = 'ACCEPTED'
        ORDER BY u.name;
        """;

        List<UserMini> list = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            ps.setInt(2, currentUserId);
            ps.setInt(3, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new UserMini(rs.getInt("id"), rs.getString("name")));
                }
            }
        }
        return list;
    }

    public List<UserMini> getIncomingRequests(int currentUserId) throws SQLException {
        String sql = """
        SELECT u.id, u.name
        FROM friend f
        JOIN `user` u ON u.id = f.user_id
        WHERE f.friend_id = ?
          AND f.status = 'PENDING'
        ORDER BY f.created_at DESC;
        """;

        return getUserMinis(currentUserId, sql);
    }

    private List<UserMini> getUserMinis(int currentUserId, String sql) throws SQLException {
        List<UserMini> list = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new UserMini(rs.getInt("id"), rs.getString("name")));
                }
            }
        }
        return list;
    }

    public List<UserMini> getOutgoingRequests(int currentUserId) throws SQLException {
        String sql = """
        SELECT u.id, u.name
        FROM friend f
        JOIN `user` u ON u.id = f.friend_id
        WHERE f.user_id = ?
          AND f.status = 'PENDING'
        ORDER BY f.created_at DESC;
        """;

        return getUserMinis(currentUserId, sql);
    }

}