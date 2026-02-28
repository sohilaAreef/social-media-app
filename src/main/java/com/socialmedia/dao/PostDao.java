package com.socialmedia.dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.socialmedia.config.DatabaseConfig;
import com.socialmedia.models.Post;
import com.socialmedia.models.FeedPost;

public class PostDao {
   
    public void createPost(Post post) throws SQLException {
        String sql = "INSERT INTO `post`(user_id, content, img, privacy) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, post.getUserId());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getImg());
            ps.setString(4, post.getPrivacy());
            ps.executeUpdate();
        }
    }

    public List<FeedPost> getFeedPosts(int limit, int offset) throws SQLException {
        List<FeedPost> posts = new ArrayList<>();

        String sql = """
        SELECT
            p.id AS post_id,
            p.user_id,
            u.name AS user_name,
            p.content,
            p.img,
            p.created_at
        FROM `post` p
        JOIN `user` u ON u.id = p.user_id
        ORDER BY p.created_at DESC
        LIMIT ? OFFSET ?;
        """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FeedPost fp = new FeedPost(
                            rs.getInt("post_id"),
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getString("content"),
                            rs.getString("img"),
                            rs.getTimestamp("created_at")
                    );
                    posts.add(fp);
                }
            }
        }

        return posts;
    }
}