package com.socialmedia.services;

import com.socialmedia.dao.CommentDao;
import com.socialmedia.dao.PostDao;
import com.socialmedia.models.Comment;
import com.socialmedia.models.Post;

import java.sql.SQLException;
import java.util.List;

public class CommentService {
    private final CommentDao commentDao = new CommentDao();
    private final PostDao postDao = new PostDao();
    private final NotificationService notificationService = new NotificationService();

    public void addComment(int userId, int postId, String content) throws SQLException {
        commentDao.addComment(userId, postId, content);
        // Send notification to the post owner
        Post post = postDao.getPost(postId);
        if (post != null && post.getUserId() != userId) {
            notificationService.sendNotification(userId, post.getUserId(), postId, "COMMENT");
        }
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