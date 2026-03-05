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
            p.created_at,
            p.privacy
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
                    fp.setPrivacy(rs.getString("privacy"));
                    posts.add(fp);
                }
            }
        }

        return posts;
    }

    public List<FeedPost> getMainFeed(int viewerUserId, int limit, int offset) throws SQLException {
        List<FeedPost> posts = new ArrayList<>();

        String sql = """
        SELECT
            p.id AS post_id,
            p.user_id,
            u.name AS user_name,
            p.content,
            p.img,
            p.privacy,
            p.created_at
        FROM `post` p
        JOIN `user` u ON u.id = p.user_id
        WHERE
            (
                p.user_id = ?
            )
            OR
            (
                p.user_id <> ? AND p.privacy = 'PUBLIC'
            )
            OR
            (
                p.user_id <> ? 
                AND p.privacy = 'FRIENDS'
                AND EXISTS (
                    SELECT 1
                    FROM friend f
                    WHERE f.status = 'ACCEPTED'
                      AND (
                          (f.user_id = ? AND f.friend_id = p.user_id)
                       OR (f.friend_id = ? AND f.user_id = p.user_id)
                      )
                )
            )
        ORDER BY p.created_at DESC
        LIMIT ? OFFSET ?;
        """;

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, viewerUserId);
            ps.setInt(2, viewerUserId);
            ps.setInt(3, viewerUserId);
            ps.setInt(4, viewerUserId);
            ps.setInt(5, viewerUserId);
            ps.setInt(6, limit);
            ps.setInt(7, offset);

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
                    fp.setPrivacy(rs.getString("privacy"));
                    posts.add(fp);
                }
            }
        }

        return posts;
    }

    public List<com.socialmedia.models.FeedPost> getUserPostsForViewer(int profileUserId, int viewerUserId, boolean isFriend, int limit, int offset) throws SQLException {
        boolean isSelf = profileUserId == viewerUserId;
        String sql = """
        SELECT p.id AS post_id,
               p.user_id,
               u.name AS user_name,
               p.content,
               p.img,
               p.privacy,
               p.created_at
        FROM `post` p
        JOIN `user` u ON u.id = p.user_id
        WHERE p.user_id = ?
          AND (
                ? = 1
             OR p.privacy = 'PUBLIC'
             OR (p.privacy = 'FRIENDS' AND ? = 1)
          )
        ORDER BY p.created_at DESC
        LIMIT ? OFFSET ?;
        """;

        List<com.socialmedia.models.FeedPost> posts = new ArrayList<>();

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, profileUserId);
            ps.setInt(2, isSelf ? 1 : 0);
            ps.setInt(3, isFriend ? 1 : 0);
            ps.setInt(4, limit);
            ps.setInt(5, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.socialmedia.models.FeedPost fp = new com.socialmedia.models.FeedPost();
                    fp.setPostId(rs.getInt("post_id"));
                    fp.setUserId(rs.getInt("user_id"));
                    fp.setUserName(rs.getString("user_name"));
                    fp.setContent(rs.getString("content"));
                    fp.setImg(rs.getString("img"));
                    fp.setPrivacy(rs.getString("privacy"));
                    fp.setCreatedAt(rs.getTimestamp("created_at"));
                    posts.add(fp);
                }
            }
        }
        return posts;
    }

    public boolean deletePost(int postId, int userId) throws SQLException {

        String sql = "DELETE FROM `post` WHERE id = ? AND user_id = ?";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, postId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }
}