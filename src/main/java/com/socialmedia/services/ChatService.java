package com.socialmedia.services;

import com.socialmedia.dao.ChatDao;
import com.socialmedia.models.Message;

import java.sql.SQLException;
import java.util.List;

public class ChatService {
    private final ChatDao chatDao = new ChatDao();

    public Message send(int senderId, int receiverId, String content) throws SQLException {
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("Empty message");
        return chatDao.insertMessage(senderId, receiverId, content.trim());
    }

    public List<Message> loadConversation(int userA, int userB) throws SQLException {
        return chatDao.getConversation(userA, userB, 200, 0);
    }

    public void markRead(int receiverId, int senderId) throws SQLException {
        chatDao.markReadFromSender(receiverId, senderId);
    }
}