package com.socialmedia.dao;

import com.socialmedia.config.DatabaseConfig;
import com.socialmedia.models.Notification;

import java.sql.*;

public class NotificationDao {
	
	public void createNotification(int senderId, int receiverId, int referenceId, String type) throws SQLException{
		String sql="INSERT INTO notification (sender_id, receiver_id, reference_id, type) VALUES (?, ?, ?, ?)";
		try(Connection con=DatabaseConfig.getConnection();
			PreparedStatement ps=con.prepareStatement(sql)){
			ps.setInt(1, senderId);
			ps.setInt(2, receiverId);
			ps.setInt(3, referenceId);
			ps.setString(4, type);
			ps.executeUpdate();
		}
	}
	
	public Notification getUserNotifications(int userId) throws SQLException{
		String sql="SELECT * FROM notification WHERE receiver_id=? ORDER BY created_at DESC";
		try(Connection con=DatabaseConfig.getConnection();
			PreparedStatement ps=con.prepareStatement(sql)){
			ps.setInt(1, userId);
			try(ResultSet rs=ps.executeQuery()){
				if (!rs.next()) return null;
				
					return new Notification(
							rs.getInt("id"),
							rs.getInt("sender_id"),
							rs.getInt("receiver_id"),
							rs.getInt("reference_id"),
							rs.getString("type"),
							rs.getBoolean("is_read"),
							rs.getTimestamp("created_at")
					);
				}
			}
		}
	
	public void deleteNotification(int notificationId) throws SQLException{
		String sql= "DELETE FROM notification WHERE id=?";
		try(Connection con=DatabaseConfig.getConnection();
			PreparedStatement ps=con.prepareStatement(sql)){
			ps.setInt(1, notificationId);
			ps.executeUpdate();
		}
	}
	
	public void markAsRead(int notificationId) throws SQLException{
		String sql="Update notification SET is_read=TRUE WHERE id=?";
			try(Connection con=DatabaseConfig.getConnection();
			PreparedStatement ps=con.prepareStatement(sql)){
			ps.setInt(1, notificationId);
			ps.executeUpdate();
		}
	}
	
	public int getUnreadCount(int userId) throws SQLException{
		String sql="SELECT COUNT(*) as count FROM notification WHERE receiver_id=? AND is_read=FALSE";
		try(Connection con=DatabaseConfig.getConnection();
			PreparedStatement ps=con.prepareStatement(sql)){
			ps.setInt(1, userId);
			try(ResultSet rs=ps.executeQuery()){
				if (rs.next()) {
					return rs.getInt("count");
				}
				return 0;
			}
		}
	}
	
	public java.util.List<Notification> getAllNotifications(int userId) throws SQLException{
		String sql="SELECT * FROM notification WHERE receiver_id=? ORDER BY created_at DESC LIMIT 20";
		java.util.List<Notification> notifications = new java.util.ArrayList<>();
		try(Connection con=DatabaseConfig.getConnection();
			PreparedStatement ps=con.prepareStatement(sql)){
			ps.setInt(1, userId);
			try(ResultSet rs=ps.executeQuery()){
				while (rs.next()) {
					notifications.add(new Notification(
							rs.getInt("id"),
							rs.getInt("sender_id"),
							rs.getInt("receiver_id"),
							rs.getInt("reference_id"),
							rs.getString("type"),
							rs.getBoolean("is_read"),
							rs.getTimestamp("created_at")
					));
				}
			}
		}
		return notifications;
	}
}
