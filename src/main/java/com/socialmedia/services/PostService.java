package com.socialmedia.services;

import java.sql.SQLException;
import com.socialmedia.dao.PostDao;
import com.socialmedia.models.Post;

public class PostService {
    private final PostDao postDao = new PostDao();

    public void addNewPost(int userId, String content, String img, String privacy) throws SQLException {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }

        String p = (privacy == null || privacy.isBlank()) ? "PUBLIC" : privacy;

        Post post = new Post(userId, content, img);
        post.setPrivacy(p);

        postDao.createPost(post);
    }
}
