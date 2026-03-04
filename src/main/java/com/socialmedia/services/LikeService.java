package com.socialmedia.services;

import com.socialmedia.dao.LikeDao;
import com.socialmedia.dao.PostDao;
import com.socialmedia.models.Post;

import java.sql.SQLException;

public class LikeService {

    private final LikeDao likeDao = new LikeDao();
    private final PostDao postDao = new PostDao();
    private final NotificationService notificationService = new NotificationService();

    public boolean toggleLike(int userId, int postId) throws SQLException {
        boolean liked = likeDao.isLiked(userId, postId);

        if (liked) {
            likeDao.removeLike(userId, postId);
            return false;
        } else {
            likeDao.addLike(userId, postId);
            // Send notification to the post owner
            Post post = postDao.getPost(postId);
            if (post != null && post.getUserId() != userId) {
                notificationService.sendNotification(userId, post.getUserId(), postId, "LIKE");
            }
            return true;
        }
    }

    public boolean isLiked(int userId, int postId) throws SQLException {
        return likeDao.isLiked(userId, postId);
    }

    public int countLikes(int postId) throws SQLException {
        return likeDao.countLikes(postId);
    }
}