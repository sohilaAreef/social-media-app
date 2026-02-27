package com.socialmedia.dao;

import com.socialmedia.config.DatabaseConfig;
import com.socialmedia.models.User;

import java.sql.*;

public class UserDao {

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM `user` WHERE email = ? LIMIT 1";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT id, name, email, password FROM `user` WHERE email = ? LIMIT 1";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User create(String name, String email, String passwordHashed) {
        String sql = "INSERT INTO `user` (name, email, password) VALUES (?,?,?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, passwordHashed);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new RuntimeException("Failed to get generated user id");
                int id = keys.getInt(1);
                return new User(id, name.trim(), email.trim().toLowerCase(), passwordHashed);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User findById(int id) {
        String sql = "SELECT id, name, email FROM `user` WHERE id = ? LIMIT 1";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            User u = new User(rs.getString("email"), rs.getString("name"), "");
            u.setId(rs.getInt("id"));
            return u;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}