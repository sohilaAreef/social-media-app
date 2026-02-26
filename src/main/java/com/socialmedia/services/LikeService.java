package com.socialmedia.services;

import com.socialmedia.dao.LikeDao;
import java.sql.SQLException;

public class LikeService {

    private final LikeDao likeDao = new LikeDao();

    public boolean toggleLike(int userId, int postId) throws SQLException {
        boolean liked = likeDao.isLiked(userId, postId);

        if (liked) {
            likeDao.removeLike(userId, postId);
            return false;
        } else {
            likeDao.addLike(userId, postId);
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