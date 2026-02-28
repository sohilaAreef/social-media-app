package com.socialmedia.app;

import com.socialmedia.config.DatabaseConfig;
import com.socialmedia.utils.PasswordHasher;

import java.sql.*;
import java.util.*;

public class SeedRunner {

    private static final int N = 30;
    private static final Random R = new Random(42);

    public static void main(String[] args) {
        try (Connection con = DatabaseConfig.getConnection()) {
            con.setAutoCommit(false);

            createTables(con);

            if (!isTableEmpty(con)) {
                System.out.println("✅ Seed already exists (users table not empty). Skipping seeding.");
                con.rollback();
                return;
            }

            seed(con);

            con.commit();
            System.out.println("✅ Seed completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Seed failed.");
        }
    }

    // -------------------- DDL --------------------

    private static void createTables(Connection con) throws SQLException {
        List<String> ddl = List.of(
                """
                CREATE TABLE IF NOT EXISTS `user`(
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(60) NOT NULL,
                    email VARCHAR(120) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """,
                """
                CREATE TABLE IF NOT EXISTS `profile`(
                    user_id INT PRIMARY KEY,
                    img VARCHAR(255),
                    bio VARCHAR(200),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """,
                """
                CREATE TABLE IF NOT EXISTS `friend`(
                    friend_id INT,
                    user_id INT,
                    status ENUM('PENDING', 'ACCEPTED', 'DECLINED') DEFAULT 'PENDING',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, friend_id),
                    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
                    FOREIGN KEY (friend_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """,
                """
                CREATE TABLE IF NOT EXISTS `post`(
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    img VARCHAR(255),
                    content TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    user_id INT,
                    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """,
                """
                CREATE TABLE IF NOT EXISTS `comment`(
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    content TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    post_id INT,
                    user_id INT,
                    FOREIGN KEY (post_id) REFERENCES `post`(id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """,
                """
                CREATE TABLE IF NOT EXISTS `likes`(
                    post_id INT,
                    user_id INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, post_id),
                    FOREIGN KEY (post_id) REFERENCES `post`(id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """
        );

        try (Statement st = con.createStatement()) {
            for (String sql : ddl) st.execute(sql);
        }
        System.out.println("✅ Tables ensured.");
    }

    private static boolean isTableEmpty(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + "`user`";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }

    // -------------------- SEED --------------------

    private static void seed(Connection con) throws SQLException {
        // 1) Users (30)
        List<Integer> userIds = new ArrayList<>();
        for (int i = 1; i <= N; i++) {
            String name = randomFullName();
            String email = ("user" + i + "@test.com").toLowerCase();
            // PasswordHasher.hash("123456"))
            String passwordHash = PasswordHasher.hash("123456");
            userIds.add(insertUser(con, name, email, passwordHash));
        }
        System.out.println("✅ Inserted users: " + userIds.size());

        // 2) Profiles (30)
        for (int i = 0; i < N; i++) {
            int uid = userIds.get(i);
            String img = "images/default-avatar.png";
            String bio = randomBio();
            insertProfile(con, uid, img, bio);
        }
        System.out.println("✅ Inserted profiles: " + N);

        // 3) Posts (30)  (واحد لكل user عشان يبقى “30 صف في جدول post”)
        List<Integer> postIds = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            int uid = userIds.get(i);
            String img = (R.nextInt(4) == 0) ? "images/post-" + (R.nextInt(5) + 1) + ".png" : null; // أحيانًا صورة
            String content = randomPostContent();
            postIds.add(insertPost(con, uid, img, content));
        }
        System.out.println("✅ Inserted posts: " + postIds.size());

        // 4) Comments (30)  (كل تعليق على بوست عشوائي، من يوزر عشوائي)
        for (int i = 0; i < N; i++) {
            int postId = postIds.get(R.nextInt(postIds.size()));
            int uid = userIds.get(R.nextInt(userIds.size()));
            String content = randomComment();
            insertComment(con, postId, uid, content);
        }
        System.out.println("✅ Inserted comments: " + N);

        // 5) Likes (30)  (مع مراعاة الـ PRIMARY KEY (user_id, post_id) ما يتكرر)
        Set<String> likePairs = new HashSet<>();
        int likesInserted = 0;
        while (likesInserted < N) {
            int postId = postIds.get(R.nextInt(postIds.size()));
            int uid = userIds.get(R.nextInt(userIds.size()));
            String key = uid + ":" + postId;
            if (likePairs.add(key)) {
                insertLike(con, postId, uid);
                likesInserted++;
            }
        }
        System.out.println("✅ Inserted likes: " + N);

        // 6) Friends (30) (علاقات unique بسبب PRIMARY KEY (user_id, friend_id))
        Set<String> friendPairs = new HashSet<>();
        int friendsInserted = 0;
        String[] statuses = {"PENDING", "ACCEPTED", "DECLINED"};

        while (friendsInserted < N) {
            int u = userIds.get(R.nextInt(userIds.size()));
            int f = userIds.get(R.nextInt(userIds.size()));
            if (u == f) continue;

            String key = u + ":" + f;
            if (friendPairs.add(key)) {
                String status = statuses[R.nextInt(statuses.length)];
                insertFriend(con, u, f, status);
                friendsInserted++;
            }
        }
        System.out.println("✅ Inserted friends: " + N);
    }

    // -------------------- INSERT HELPERS --------------------

    private static int insertUser(Connection con, String name, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO `user`(name,email,password) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static void insertProfile(Connection con, int userId, String img, String bio) throws SQLException {
        String sql = "INSERT INTO `profile`(user_id,img,bio) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, img);
            ps.setString(3, bio);
            ps.executeUpdate();
        }
    }

    private static int insertPost(Connection con, int userId, String img, String content) throws SQLException {
        String sql = "INSERT INTO `post`(user_id,img,content) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, img);
            ps.setString(3, content);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static void insertComment(Connection con, int postId, int userId, String content) throws SQLException {
        String sql = "INSERT INTO `comment`(post_id,user_id,content) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    private static void insertLike(Connection con, int postId, int userId) throws SQLException {
        String sql = "INSERT INTO `likes`(post_id,user_id) VALUES (?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private static void insertFriend(Connection con, int userId, int friendId, String status) throws SQLException {
        String sql = "INSERT INTO `friend`(user_id, friend_id, status) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            ps.setString(3, status);
            ps.executeUpdate();
        }
    }

    // -------------------- FAKE DATA GENERATORS --------------------

    private static String randomFullName() {
        String[] first = {"Ahmed", "Mona", "Omar", "Yara", "Hassan", "Nour", "Salma", "Karim", "Nada", "Mostafa"};
        String[] last = {"Ehab", "Ali", "Mahmoud", "Ibrahim", "Hamed", "Saeed", "Fathy", "Adel", "Kamel", "Samir"};
        return first[R.nextInt(first.length)] + " " + last[R.nextInt(last.length)];
    }

    private static String randomBio() {
        String[] bios = {
                "Frontend dev & coffee lover ☕",
                "JavaFX enjoyer. Building clean UIs.",
                "Gym + coding = balance 💪",
                "Learning SQL and OOP daily.",
                "Design systems & pixel-perfect UI."
        };
        return bios[R.nextInt(bios.length)];
    }

    private static String randomPostContent() {
        String[] posts = {
                "Hello world! My first post 🚀",
                "Just finished a JavaFX screen, looks clean.",
                "Database relationships are finally working ✅",
                "Tip: keep SQL inside DAO only.",
                "Working on pagination for the news feed.",
                "What’s your favorite programming language?"
        };
        return posts[R.nextInt(posts.length)];
    }

    private static String randomComment() {
        String[] comments = {
                "Nice one 🔥",
                "Totally agree ✅",
                "Well done!",
                "Keep going 💪",
                "Great progress!"
        };
        return comments[R.nextInt(comments.length)];
    }
}