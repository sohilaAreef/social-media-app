package com.socialmedia.services;

import java.sql.SQLException;
import java.util.List;

import com.socialmedia.dao.PostDao;
import com.socialmedia.models.Post;

public class PostService {
    private final PostDao postDao = new PostDao();

    public void addNewPost(int userId, String content, String img) throws SQLException{
        if(content == null || content.trim().isEmpty()){
            throw new IllegalArgumentException("Post conent cannot be empty");
        }
        Post post = new Post(userId, content, img);
        postDao.createPost(post);
    }
    public List<Post> getNewsFeed() throws SQLException {
        return postDao.getAllPosts();
    }
}
