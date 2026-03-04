package com.socialmedia.services;

import com.socialmedia.dao.NotificationDao;
import com.socialmedia.models.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {
	
	private final NotificationDao notificationDao= new NotificationDao();
    
	public Notification getUserNotifications(int userId) throws SQLException{
		 return notificationDao.getUserNotifications(userId);
		}
	
	public void sendNotification(int senderId, int receiverId, int referenceId, String type) throws SQLException {
		notificationDao.createNotification(senderId, receiverId, referenceId, type);
	}
	
	public int getUnreadCount(int userId) throws SQLException {
		return notificationDao.getUnreadCount(userId);
	}
	
	public List<Notification> getAllNotifications(int userId) throws SQLException {
		return notificationDao.getAllNotifications(userId);
	}
	
	public void markAsRead(int notificationId) throws SQLException {
		notificationDao.markAsRead(notificationId);
	}
}
