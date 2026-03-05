package com.socialmedia.services;

import com.socialmedia.dao.PostDao;
import com.socialmedia.dao.UserDao;
import com.socialmedia.dao.FriendDao;
import com.socialmedia.models.FeedPost;

import java.sql.SQLException;
import java.util.List;

public class SearchService {
    private final UserDao userDao = new UserDao();
    private final PostDao postDao = new PostDao();

    public List<FriendDao.UserMini> searchUsers(String q) throws SQLException {
        return userDao.searchUsers(q, 30);
    }

    public List<FeedPost> searchPosts(String q, int viewerUserId) throws SQLException {
        return postDao.searchPostsForViewer(q, viewerUserId, 30);
    }
}