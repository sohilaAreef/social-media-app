package com.socialmedia.dao;

import com.socialmedia.config.DatabaseConfig;
import com.socialmedia.models.Profile;
import com.socialmedia.models.User;
import com.socialmedia.models.UserProfile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    public UserProfile getProfile(int userId) {
    	String sql= """
                SELECT u.id, u.name, u.email, u.created_at,
                p.img, p.bio
         FROM user u
         INNER JOIN profile p ON u.id = p.user_id
         WHERE u.id = ?
         """;
    	try (Connection con= DatabaseConfig.getConnection();
    		 PreparedStatement ps= con.prepareStatement(sql)){
    		
    		ps.setInt(1,userId);
    		ResultSet rs=ps.executeQuery();
    		                     
    		if (!rs.next()) return null; 
    		
             return new UserProfile(
                rs.getInt("id"),	 
    			rs.getString("name"),
                rs.getString("email"),
                rs.getString("img"),
                rs.getString("bio"),
                rs.getTimestamp("created_at")
    		);
             
    	}catch (SQLException e) {
    		throw new RuntimeException(e);
    	}
    			
    }
    
    public boolean updateProfile(User u, Profile p, String newHashedPassword) {
        boolean updatePassword = (newHashedPassword != null && !newHashedPassword.isEmpty());

        String updateUserSql;
        if (updatePassword) {
            updateUserSql = "UPDATE user SET name=?, password=? WHERE id=?";
        } else {
            updateUserSql = "UPDATE user SET name=? WHERE id=?";
        }

        String updateProfileSql = "UPDATE profile SET img=?, bio=? WHERE user_id=?";

        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps1 = con.prepareStatement(updateUserSql)) {
                    ps1.setString(1, u.getName());

                    if (updatePassword) {
                        ps1.setString(2, newHashedPassword);
                        ps1.setInt(3, u.getId());
                    } else {
                        ps1.setInt(2, u.getId());
                    }

                    ps1.executeUpdate();
                }

               
                try (PreparedStatement ps2 = con.prepareStatement(updateProfileSql)) {
                    ps2.setString(1, p.getImg());
                    ps2.setString(2, p.getBio());
                    ps2.setInt(3, p.getUserId());
                    ps2.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}