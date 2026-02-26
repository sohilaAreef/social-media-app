package com.socialmedia.services;

import java.sql.SQLException;
import java.util.List;

import com.socialmedia.dao.PostDao;
import com.socialmedia.models.FeedPost;

public class FeedService {
    private final PostDao postDao = new PostDao();

    public List<FeedPost> loadPage(int page, int pageSize) throws SQLException {
        int offset = page * pageSize;
        return postDao.getFeedPosts(pageSize, offset);
    }
}