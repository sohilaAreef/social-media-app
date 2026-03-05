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

            if (!isUsersTableEmpty(con)) {
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
                // ---------------- user ----------------
                """
                CREATE TABLE IF NOT EXISTS `user`(
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(60) NOT NULL,
                    email VARCHAR(120) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """,

                // ---------------- profile ----------------
                """
                CREATE TABLE IF NOT EXISTS `profile`(
                    user_id INT PRIMARY KEY,
                    img VARCHAR(255),
                    bio VARCHAR(200),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """,

                // ---------------- friend ----------------
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

                // ---------------- post (WITH privacy) ----------------
                """
                CREATE TABLE IF NOT EXISTS `post`(
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    img VARCHAR(255),
                    content TEXT,
                    privacy ENUM('PUBLIC','FRIENDS','PRIVATE') NOT NULL DEFAULT 'PUBLIC',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    user_id INT,
                    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """,

                // ---------------- comment ----------------
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

                // ---------------- likes ----------------
                """
                CREATE TABLE IF NOT EXISTS `likes`(
                    post_id INT,
                    user_id INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, post_id),
                    FOREIGN KEY (post_id) REFERENCES `post`(id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """,

                // ---------------- notification ----------------
                // type examples: LIKE, COMMENT, FRIEND_REQUEST
                """
                CREATE TABLE IF NOT EXISTS `notification`(
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    sender_id INT NOT NULL,
                    receiver_id INT NOT NULL,
                    reference_id INT,
                    type VARCHAR(30) NOT NULL,
                    is_read BOOLEAN NOT NULL DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (sender_id) REFERENCES `user`(id) ON DELETE CASCADE,
                    FOREIGN KEY (receiver_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """,

                // ---------------- message (chat) ----------------
                """
                CREATE TABLE IF NOT EXISTS `message`(
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    sender_id INT NOT NULL,
                    receiver_id INT NOT NULL,
                    content TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (sender_id) REFERENCES `user`(id) ON DELETE CASCADE,
                    FOREIGN KEY (receiver_id) REFERENCES `user`(id) ON DELETE CASCADE
                );
                """
        );

        try (Statement st = con.createStatement()) {
            for (String sql : ddl) st.execute(sql);
        }
        System.out.println("✅ Tables ensured.");
    }

    private static boolean isUsersTableEmpty(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `user`";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }

    // -------------------- SEED --------------------

    private static void seed(Connection con) throws SQLException {
        // 1) Users
        List<Integer> userIds = new ArrayList<>();
        for (int i = 1; i <= N; i++) {
            String name = randomFullName();
            String email = ("user" + i + "@test.com").toLowerCase();
            String passwordHash = PasswordHasher.hash("123456");
            userIds.add(insertUser(con, name, email, passwordHash));
        }
        System.out.println("✅ Inserted users: " + userIds.size());

        // 2) Profiles
        for (int i = 0; i < N; i++) {
            int uid = userIds.get(i);
            // خليها null أو path لو انت بتخزن absolute path
            // لو بتستخدم Resource default-avatar داخل UI يبقى خليه null أفضل
            String img = null;
            String bio = randomBio();
            insertProfile(con, uid, img, bio);
        }
        System.out.println("✅ Inserted profiles: " + N);

        // 3) Posts (with privacy)
        List<Integer> postIds = new ArrayList<>();
        String[] privacies = {"PUBLIC", "FRIENDS", "PRIVATE"};

        for (int i = 0; i < N; i++) {
            int uid = userIds.get(i);
            String img = null; // خليه null علشان الصور resources مش file paths
            String content = randomPostContent();
            String privacy = privacies[R.nextInt(privacies.length)];
            postIds.add(insertPost(con, uid, img, content, privacy));
        }
        System.out.println("✅ Inserted posts: " + postIds.size());

        // 4) Friends
        // هننشئ mix: ACCEPTED / PENDING / DECLINED
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

                // Notification for friend request لو Pending
                if ("PENDING".equals(status)) {
                    // sender=u, receiver=f
                    insertNotification(con, u, f, null, "FRIEND_REQUEST", false);
                }

                friendsInserted++;
            }
        }
        System.out.println("✅ Inserted friends: " + N);

        // 5) Comments (random)
        for (int i = 0; i < N; i++) {
            int postId = postIds.get(R.nextInt(postIds.size()));
            int uid = userIds.get(R.nextInt(userIds.size()));
            String content = randomComment();
            int commentId = insertComment(con, postId, uid, content);

            // Notification: COMMENT -> صاحب البوست
            int postOwner = getPostOwner(con, postId);
            if (postOwner != uid) {
                insertNotification(con, uid, postOwner, commentId, "COMMENT", false);
            }
        }
        System.out.println("✅ Inserted comments: " + N);

        // 6) Likes (unique)
        Set<String> likePairs = new HashSet<>();
        int likesInserted = 0;

        while (likesInserted < N) {
            int postId = postIds.get(R.nextInt(postIds.size()));
            int uid = userIds.get(R.nextInt(userIds.size()));
            String key = uid + ":" + postId;

            if (likePairs.add(key)) {
                insertLike(con, postId, uid);
                likesInserted++;

                // Notification: LIKE -> صاحب البوست
                int postOwner = getPostOwner(con, postId);
                if (postOwner != uid) {
                    insertNotification(con, uid, postOwner, postId, "LIKE", false);
                }
            }
        }
        System.out.println("✅ Inserted likes: " + N);

        // 7) Messages (Chat) بين الناس اللي ACCEPTED
        // هنجيب كل العلاقات المقبولة ونحط messages بينهم
        List<int[]> acceptedPairs = getAcceptedFriendPairs(con);

        int messagesToInsert = Math.min(N * 2, Math.max(20, acceptedPairs.size() * 3));
        for (int i = 0; i < messagesToInsert && !acceptedPairs.isEmpty(); i++) {
            int[] pair = acceptedPairs.get(R.nextInt(acceptedPairs.size()));
            int a = pair[0];
            int b = pair[1];

            // Random direction
            int sender = (R.nextBoolean()) ? a : b;
            int receiver = (sender == a) ? b : a;

            insertMessage(con, sender, receiver, randomMessage());
        }
        System.out.println("✅ Inserted messages: " + messagesToInsert);
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

    private static int insertPost(Connection con, int userId, String img, String content, String privacy) throws SQLException {
        String sql = "INSERT INTO `post`(user_id,img,content,privacy) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, img);
            ps.setString(3, content);
            ps.setString(4, privacy);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private static int insertComment(Connection con, int postId, int userId, String content) throws SQLException {
        String sql = "INSERT INTO `comment`(post_id,user_id,content) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
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

    private static void insertNotification(Connection con, int senderId, int receiverId, Integer referenceId, String type, boolean isRead) throws SQLException {
        String sql = "INSERT INTO `notification`(sender_id, receiver_id, reference_id, type, is_read) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            if (referenceId == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, referenceId);
            ps.setString(4, type);
            ps.setBoolean(5, isRead);
            ps.executeUpdate();
        }
    }

    private static void insertMessage(Connection con, int senderId, int receiverId, String content) throws SQLException {
        String sql = "INSERT INTO `message`(sender_id, receiver_id, content) VALUES (?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, content);
            ps.executeUpdate();
        }
    }

    private static int getPostOwner(Connection con, int postId) throws SQLException {
        String sql = "SELECT user_id FROM `post` WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return -1;
                return rs.getInt(1);
            }
        }
    }

    private static List<int[]> getAcceptedFriendPairs(Connection con) throws SQLException {
        // IMPORTANT:
        // جدول friend عندك يمثل علاقة direction: (user_id -> friend_id)
        // Accept معناها إن الشخصين موافقين/في علاقة صداقة.
        // لو عندك نظامك بيحفظ صف واحد فقط بعد accept -> خلاص كفاية
        // لو بيحفظ صفين متعاكسين -> برضه شغال.
        String sql = "SELECT user_id, friend_id FROM `friend` WHERE status = 'ACCEPTED'";
        List<int[]> pairs = new ArrayList<>();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                pairs.add(new int[]{rs.getInt(1), rs.getInt(2)});
            }
        }
        return pairs;
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

    private static String randomMessage() {
        String[] msgs = {
                "Hey! how are you?",
                "Shall we work on the project tonight?",
                "Nice post 😂",
                "Congrats! 🔥",
                "Let’s catch up later.",
                "Are you free now?"
        };
        return msgs[R.nextInt(msgs.length)];
    }
}