package com.socialmedia.dao;

import com.socialmedia.config.DatabaseConfig;
import com.socialmedia.models.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatDao {

    public Message insertMessage(int senderId, int receiverId, String content) throws SQLException {
        String sql = "INSERT INTO message(sender_id, receiver_id, content) VALUES (?,?,?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, content);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated id");
                int id = keys.getInt(1);
                Message m = new Message(senderId, receiverId, content);
                m.setId(id);
                m.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                m.setRead(false);
                return m;
            }
        }
    }

    public List<Message> getConversation(int userA, int userB, int limit, int offset) throws SQLException {
        String sql = """
            SELECT id, sender_id, receiver_id, content, created_at, is_read
            FROM message
            WHERE (sender_id=? AND receiver_id=?)
               OR (sender_id=? AND receiver_id=?)
            ORDER BY created_at ASC
            LIMIT ? OFFSET ?;
        """;

        List<Message> list = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userA);
            ps.setInt(2, userB);
            ps.setInt(3, userB);
            ps.setInt(4, userA);
            ps.setInt(5, limit);
            ps.setInt(6, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setId(rs.getInt("id"));
                    m.setSenderId(rs.getInt("sender_id"));
                    m.setReceiverId(rs.getInt("receiver_id"));
                    m.setContent(rs.getString("content"));
                    m.setCreatedAt(rs.getTimestamp("created_at"));
                    m.setRead(rs.getBoolean("is_read"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    public void markReadFromSender(int receiverId, int senderId) throws SQLException {
        String sql = "UPDATE message SET is_read=1 WHERE receiver_id=? AND sender_id=? AND is_read=0";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, receiverId);
            ps.setInt(2, senderId);
            ps.executeUpdate();
        }
    }
}