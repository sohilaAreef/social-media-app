package com.socialmedia.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.socialmedia.config.DatabaseConfig;
import com.socialmedia.models.Post;

public class PostDao {
   
    public void createPost(Post post) throws SQLException {
        String sql = "INSERT INTO `post`(user_id, content, img) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, post.getUserId());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getImg());
            ps.executeUpdate();
        }
    }

   
    public List<Post> getAllPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM `post` ORDER BY created_at DESC";
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Post p = new Post();
                p.setId(rs.getInt("id"));
                p.setUserId(rs.getInt("user_id"));
                p.setContent(rs.getString("content"));
                p.setImg(rs.getString("img"));
                p.setCreatedAt(rs.getTimestamp("created_at"));
                posts.add(p);
            }
        }
        return posts;
    }
}