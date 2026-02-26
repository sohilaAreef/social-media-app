package com.socialmedia.services;

import com.socialmedia.dao.CommentDao;
import com.socialmedia.models.Comment;
import java.sql.SQLException;
import java.util.List;

public class CommentService {
    private final CommentDao commentDao = new CommentDao();

    public void addComment(int userId, int postId, String content) throws SQLException {
        commentDao.addComment(userId, postId, content);
    }

    public int countComments(int postId) throws SQLException {
        return commentDao.countComments(postId);
    }

    public List<Comment> getLatestComments(int postId, int limit) throws SQLException {
        return commentDao.getLatestComments(postId, limit);
    }

    public List<Comment> getAllComments(int postId) throws SQLException {
        return commentDao.getAllComments(postId);
    }
}