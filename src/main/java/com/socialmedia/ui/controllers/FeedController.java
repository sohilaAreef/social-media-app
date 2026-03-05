package com.socialmedia.ui.controllers;

import java.sql.SQLException;
import java.util.List;
import com.socialmedia.app.Navigator;
import com.socialmedia.dao.FriendDao;
import com.socialmedia.services.*;
import com.socialmedia.models.Comment;
import com.socialmedia.models.Notification;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import com.socialmedia.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Side;
import com.socialmedia.models.FeedPost;
import com.socialmedia.utils.TimeAgo;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

public class FeedController {

    @FXML
    private TextArea postContentArea;

    @FXML
    private VBox postsContainer;

    @FXML
    private ScrollPane feedScroll;

    @FXML
    private TextField searchField;

    @FXML
    private Button notificationBtn;

    @FXML
    private Label notificationBadge;

    @FXML
    private ComboBox<String> privacyBox;

    private final FeedService feedService = new FeedService();
    private final PostService postService = new PostService();
    private final AuthService authService = new AuthService();
    private final LikeService likeService = new LikeService();
    private final CommentService commentService = new CommentService();
    private final FriendService friendService = new FriendService();
    private final NotificationService notificationService = new NotificationService();
    private final SearchService searchService = new SearchService();

    private int page = 0;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @FXML
    public void initialize() {

        if (Session.getCurrentUser() == null) {
            Navigator.goToLogin();
            return;
        }

        if (privacyBox != null) {
            if (privacyBox.getItems().isEmpty()) {
                privacyBox.getItems().addAll("PUBLIC", "FRIENDS", "PRIVATE");
            }
            if (privacyBox.getValue() == null) privacyBox.setValue("PUBLIC");
        }

        loadNextPage();
        loadNotificationCount();

        feedScroll.vvalueProperty().addListener((obs, oldV, newV) -> {
            if (!hasMore || isLoading) return;
            if (newV.doubleValue() > 0.92) {
                loadNextPage();
            }
        });
    }

    @FXML
    private void handlePostAction() {
        String content = postContentArea.getText();

        if (content == null || content.trim().isEmpty()) {
            System.out.println("Warning: Cannot publish an empty post.");
            return;
        }

        try {

            if (Session.getCurrentUser() != null) {
                int currentUserId = Session.getCurrentUser().getId();

                postService.addNewPost(currentUserId, content, null);

                postContentArea.clear();
                System.out.println("Post published successfully!");
                onRefresh();
            } else {
                System.err.println("Error: Session is null. Please login first.");
            }

        } catch (SQLException e) {
            System.err.println("Error: Failed to publish post: " + e.getMessage());
        }
    }

    private void loadNextPage() {
        if (isLoading || !hasMore) return;
        isLoading = true;

        new Thread(() -> {
            try {
                int viewerId = Session.getCurrentUser().getId();
                List<FeedPost> posts = feedService.loadMainFeedPage(viewerId, page, pageSize);

                Platform.runLater(() -> {
                    if (posts.isEmpty()) {
                        hasMore = false;
                    } else {
                        for (FeedPost p : posts) {
                            postsContainer.getChildren().add(createPostCard(p));
                        }
                        page++;
                    }
                    isLoading = false;
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> isLoading = false);
            }
        }).start();
    }

    private Node createPostCard(FeedPost post) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);
        """);
        Label name = new Label(post.getUserName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #111;");

        name.setOnMouseEntered(e -> name.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1877f2;"));
        name.setOnMouseExited(e -> name.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #111;"));

        name.setOnMouseClicked(e -> Navigator.goToUserProfile(post.getUserId()));

        Label time = new Label(TimeAgo.from(post.getCreatedAt()));
        time.setStyle("-fx-text-fill: #65676b; -fx-font-size: 11;");

        VBox header = new VBox(2, name, time);

        Label content = new Label(post.getContent() == null ? "" : post.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13; -fx-text-fill: #111;");

        card.getChildren().addAll(header, content);

        if (post.getImg() != null && !post.getImg().isBlank()) {
            ImageView iv = new ImageView();
            iv.setPreserveRatio(true);
            iv.setFitWidth(760);
            iv.setSmooth(true);

            try {
                Image img = new Image("file:" + post.getImg(), true);
                iv.setImage(img);
                card.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        HBox actions = new HBox(10);
        actions.setPadding(new Insets(8, 0, 0, 0));
        actions.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        // ------------------------- Like Btn -------------------------------------------//
        Button likeBtn = new Button();
        likeBtn.setStyle("-fx-background-color: transparent; -fx-font-weight: bold;");

        int currentUserId = Session.getCurrentUser().getId();

        try {
            boolean liked = likeService.isLiked(currentUserId, post.getPostId());
            int count = likeService.countLikes(post.getPostId());
            applyLikeStyle(likeBtn, liked, count);
        } catch (SQLException e) {
            e.printStackTrace();
            applyLikeStyle(likeBtn, false, 0);
        }
        likeBtn.setOnAction(e -> onLike(post.getPostId(), likeBtn));

        // ------------------------- Comment Btn -------------------------------------------//
        Button commentBtn = new Button();
        commentBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-weight: bold;");
        VBox commentsBox = new VBox(6);
        commentsBox.setPadding(new Insets(6, 0, 0, 0));

        HBox commentInputRow = new HBox(8);
        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");
        commentField.setPrefWidth(500);
        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-radius: 8; -fx-background-color: #e4e6eb; -fx-font-weight: bold;");
        commentInputRow.getChildren().addAll(commentField, sendBtn);

        try {
            int count = commentService.countComments(post.getPostId());
            applyCommentText(commentBtn, count);

            renderInlineComments(commentsBox, post.getPostId(), count);
        } catch (SQLException e) {
            e.printStackTrace();
            applyCommentText(commentBtn, 0);
        }
        commentBtn.setOnAction(e -> commentField.requestFocus());
        sendBtn.setOnAction(e -> onSendComment(post.getPostId(), commentField, commentBtn, commentsBox));
        commentField.setOnAction(e -> onSendComment(post.getPostId(), commentField, commentBtn, commentsBox));

        actions.getChildren().addAll(likeBtn, commentBtn);
        card.getChildren().addAll(actions, commentInputRow, commentsBox);

        return card;
    }
    private void onLike(int postId, Button likeBtn) {
        try {
            int userId = Session.getCurrentUser().getId();

            boolean liked = likeService.toggleLike(userId, postId);
            int count = likeService.countLikes(postId);

            applyLikeStyle(likeBtn, liked, count);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void applyLikeStyle(Button btn, boolean liked, int count) {
        if (liked) {
            btn.setText("Liked (" + count + ")");
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-font-weight: bold;");
        } else {
            btn.setText("Like (" + count + ")");
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-weight: bold;");
        }
    }
    private void applyCommentText(Button btn, int count) {

        btn.setText("Comment (" + count + ")");
    }
    private void renderInlineComments(VBox commentsBox, int postId, int totalCount) throws SQLException {

        commentsBox.getChildren().clear();

        List<Comment> latest = commentService.getLatestComments(postId, 3);
        for (Comment c : latest) {
            commentsBox.getChildren().add(createCommentRow(c));
        }
        if (totalCount > 2) {
            Button showMore = new Button("Show more (" + totalCount + ")");
            showMore.setStyle("-fx-background-color: transparent; -fx-text-fill: #1877f2; -fx-font-weight: bold;");
            showMore.setOnAction(e -> openCommentsPopup(postId));
            commentsBox.getChildren().add(showMore);
        }
    }
    private Node createCommentRow(Comment c) {

        VBox box = new VBox(2);
        box.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 8; -fx-padding: 8;");

        Label content = new Label(c.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #111;");

        box.getChildren().add(content);
        return box;
    }
    private void onSendComment(int postId, TextField commentField, Button commentBtn, VBox commentsBox) {

        String text = commentField.getText();
        if (text == null || text.trim().isEmpty()) return;

        int userId = Session.getCurrentUser().getId();

        new Thread(() -> {
            try {
                commentService.addComment(userId, postId, text.trim());
                int count = commentService.countComments(postId);

                Platform.runLater(() -> {
                    commentField.clear();
                    applyCommentText(commentBtn, count);

                    try {
                        renderInlineComments(commentsBox, postId, count);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void openCommentsPopup(int postId) {

        Stage popup = new Stage();
        popup.setTitle("Comments");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        header.setSpacing(10);

        Label title = new Label("All Comments");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer);
        root.setTop(header);
        VBox listBox = new VBox(8);
        listBox.setPadding(new Insets(12));

        ScrollPane sp = new ScrollPane(listBox);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        root.setCenter(sp);

        Scene scene = new Scene(root, 420, 520);
        popup.setScene(scene);

        new Thread(() -> {
            try {
                List<Comment> all = commentService.getAllComments(postId);
                Platform.runLater(() -> {
                    listBox.getChildren().clear();
                    for (Comment c : all) {
                        listBox.getChildren().add(createCommentRow(c));
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();

        popup.show();
    }

    private void openFriendsModal() {
        if (Session.getCurrentUser() == null) return;

        int currentUserId = Session.getCurrentUser().getId();

        Stage modal = new Stage();
        modal.setTitle("Friends");
        modal.initModality(Modality.APPLICATION_MODAL);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox(10);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Friends Center");
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-radius: 10; -fx-background-color: #e4e6eb; -fx-font-weight: bold;");

        Button closeBtn = new Button("X");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> modal.close());

        header.getChildren().addAll(title, spacer, refreshBtn, closeBtn);
        root.setTop(header);

        // Tabs
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Content containers
        VBox friendsBox = new VBox(10);
        friendsBox.setPadding(new Insets(12));

        VBox incomingBox = new VBox(10);
        incomingBox.setPadding(new Insets(12));

        VBox outgoingBox = new VBox(10);
        outgoingBox.setPadding(new Insets(12));

        ScrollPane friendsSP = wrapScroll(friendsBox);
        ScrollPane incomingSP = wrapScroll(incomingBox);
        ScrollPane outgoingSP = wrapScroll(outgoingBox);

        Tab friendsTab = new Tab("Friends", friendsSP);
        Tab incomingTab = new Tab("Incoming", incomingSP);
        Tab outgoingTab = new Tab("Outgoing", outgoingSP);

        tabs.getTabs().addAll(friendsTab, incomingTab, outgoingTab);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 520, 560);
        modal.setScene(scene);

        Runnable reload = () -> loadFriendsModalData(currentUserId, friendsBox, incomingBox, outgoingBox, tabs);
        refreshBtn.setOnAction(e -> reload.run());

        // first load
        reload.run();

        modal.showAndWait();
    }

    private ScrollPane wrapScroll(VBox box) {
        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        return sp;
    }

    private void loadFriendsModalData(
            int currentUserId,
            VBox friendsBox,
            VBox incomingBox,
            VBox outgoingBox,
            TabPane tabs
    ) {
        friendsBox.getChildren().setAll(new Label("Loading..."));
        incomingBox.getChildren().setAll(new Label("Loading..."));
        outgoingBox.getChildren().setAll(new Label("Loading..."));

        new Thread(() -> {
            try {
                List<FriendDao.UserMini> friends = friendService.listFriends(currentUserId);
                List<FriendDao.UserMini> incoming = friendService.listIncoming(currentUserId);
                List<FriendDao.UserMini> outgoing = friendService.listOutgoing(currentUserId);

                Platform.runLater(() -> {
                    renderFriendsList(friendsBox, friends);
                    renderIncomingList(incomingBox, currentUserId, incoming, () -> loadFriendsModalData(currentUserId, friendsBox, incomingBox, outgoingBox, tabs));
                    renderOutgoingList(outgoingBox, currentUserId, outgoing, () -> loadFriendsModalData(currentUserId, friendsBox, incomingBox, outgoingBox, tabs));

                    tabs.getTabs().get(0).setText("Friends (" + friends.size() + ")");
                    tabs.getTabs().get(1).setText("Incoming (" + incoming.size() + ")");
                    tabs.getTabs().get(2).setText("Outgoing (" + outgoing.size() + ")");
                });

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    friendsBox.getChildren().setAll(new Label("Failed to load"));
                    incomingBox.getChildren().setAll(new Label("Failed to load"));
                    outgoingBox.getChildren().setAll(new Label("Failed to load"));
                });
            }
        }).start();
    }

    private void renderFriendsList(VBox box, List<FriendDao.UserMini> friends) {
        box.getChildren().clear();
        if (friends.isEmpty()) {
            box.getChildren().add(new Label("No friends yet."));
            return;
        }

        for (var u : friends) {
            HBox row = new HBox(10);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 10;");

            Label name = clickableName(u.name(), u.id());

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button view = new Button("View");
            view.setStyle("-fx-background-radius: 10; -fx-background-color: #e4e6eb; -fx-font-weight: bold;");
            view.setOnAction(e -> Navigator.goToUserProfile(u.id()));

            Button msg = new Button("Message");
            msg.setStyle("-fx-background-radius: 10; -fx-background-color: #1877f2; -fx-text-fill: white; -fx-font-weight: bold;");
            msg.setOnAction(e -> openChat(u.id(), u.name())); // ✅ أهم سطر

            row.getChildren().addAll(name, spacer, view, msg);
            box.getChildren().add(row);
        }
    }

    private void openChat(int userId, String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/chat.fxml"));

            Stage stage = new Stage();
            stage.setTitle("Chat - " + name);
            stage.initModality(Modality.NONE);

            Scene scene = new Scene(loader.load(), 420, 500);
            stage.setScene(scene);

            ChatController controller = loader.getController();
            controller.initChat(userId, name);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderIncomingList(VBox box, int currentUserId, List<FriendDao.UserMini> incoming, Runnable reload) {
        box.getChildren().clear();
        if (incoming.isEmpty()) {
            box.getChildren().add(new Label("No incoming requests."));
            return;
        }

        for (var u : incoming) {
            HBox row = new HBox(10);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 10;");

            Label name = clickableName(u.name(), u.id());

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button accept = new Button("Accept");
            accept.setStyle("-fx-background-color: #1877f2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
            accept.setOnAction(e -> {
                try {
                    friendService.acceptIncoming(currentUserId, u.id());
                    reload.run();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            Button decline = getButton(currentUserId, reload, u);

            row.getChildren().addAll(name, spacer, accept, decline);
            box.getChildren().add(row);
        }
    }

    private Button getButton(int currentUserId, Runnable reload, FriendDao.UserMini u) {
        Button decline = new Button("Decline");
        decline.setStyle("-fx-background-color: #e4e6eb; -fx-font-weight: bold; -fx-background-radius: 10;");
        decline.setOnAction(e -> {
            if (confirm("Decline request?", "Do you want to decline this request?")) return;
            try {
                friendService.declineIncoming(currentUserId, u.id());
                reload.run();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        return decline;
    }

    private void renderOutgoingList(VBox box, int currentUserId, List<FriendDao.UserMini> outgoing, Runnable reload) {
        box.getChildren().clear();
        if (outgoing.isEmpty()) {
            box.getChildren().add(new Label("No outgoing requests."));
            return;
        }

        for (var u : outgoing) {
            HBox row = new HBox(10);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 10;");

            Label name = clickableName(u.name(), u.id());

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button cancel = new Button("Cancel");
            cancel.setStyle("-fx-background-color: #e4e6eb; -fx-font-weight: bold; -fx-background-radius: 10;");
            cancel.setOnAction(e -> {
                if (confirm("Cancel request?", "Do you want to cancel this request?")) return;
                try {
                    friendService.cancelPending(currentUserId, u.id());
                    reload.run();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            row.getChildren().addAll(name, spacer, cancel);
            box.getChildren().add(row);
        }
    }

    private void openSearchModal(String query) {
        int viewerId = Session.getCurrentUser().getId();

        Stage modal = new Stage();
        modal.setTitle("Search");
        modal.initModality(Modality.APPLICATION_MODAL);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox(10);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Search: " + query);
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("X");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> modal.close());

        header.getChildren().addAll(title, spacer, closeBtn);
        root.setTop(header);

        // Tabs
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox usersBox = new VBox(10);
        usersBox.setPadding(new Insets(12));
        ScrollPane usersSP = wrapScroll(usersBox);

        VBox postsBox = new VBox(10);
        postsBox.setPadding(new Insets(12));
        ScrollPane postsSP = wrapScroll(postsBox);

        Tab usersTab = new Tab("Users", usersSP);
        Tab postsTab = new Tab("Posts", postsSP);

        tabs.getTabs().addAll(usersTab, postsTab);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 560, 600);
        modal.setScene(scene);

        // Load data async
        usersBox.getChildren().setAll(new Label("Loading..."));
        postsBox.getChildren().setAll(new Label("Loading..."));

        new Thread(() -> {
            try {
                var users = searchService.searchUsers(query);
                var posts = searchService.searchPosts(query, viewerId);

                Platform.runLater(() -> {
                    renderUsersResults(usersBox, users);
                    renderPostsResults(postsBox, posts);
                    usersTab.setText("Users (" + users.size() + ")");
                    postsTab.setText("Posts (" + posts.size() + ")");
                });

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    usersBox.getChildren().setAll(new Label("Failed to load users"));
                    postsBox.getChildren().setAll(new Label("Failed to load posts"));
                });
            }
        }).start();

        modal.showAndWait();
    }

    private void renderUsersResults(VBox box, List<FriendDao.UserMini> users) {
        box.getChildren().clear();
        if (users.isEmpty()) {
            box.getChildren().add(new Label("No users found."));
            return;
        }

        for (var u : users) {
            HBox row = new HBox(10);
            row.setPadding(new Insets(10));
            row.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 10;");

            Label name = clickableName(u.name(), u.id());

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button view = new Button("View");
            view.setStyle("-fx-background-radius: 10; -fx-background-color: #e4e6eb; -fx-font-weight: bold;");
            view.setOnAction(e -> Navigator.goToUserProfile(u.id()));

            row.getChildren().addAll(name, spacer, view);
            box.getChildren().add(row);
        }
    }

    private void renderPostsResults(VBox box, List<FeedPost> posts) {
        box.getChildren().clear();
        if (posts.isEmpty()) {
            box.getChildren().add(new Label("No posts found."));
            return;
        }

        for (FeedPost p : posts) {
            VBox card = new VBox(6);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 10;");

            Label name = clickableName(p.getUserName(), p.getUserId());

            Label time = new Label(TimeAgo.from(p.getCreatedAt()) + " • " + p.getPrivacy());
            time.setStyle("-fx-text-fill: #65676b; -fx-font-size: 11;");

            Label content = new Label(p.getContent() == null ? "" : p.getContent());
            content.setWrapText(true);

            card.getChildren().addAll(name, time, content);
            box.getChildren().add(card);
        }
    }

    private Label clickableName(String text, int userId) {
        Label name = new Label(text);
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #111;");
        name.setOnMouseEntered(e -> name.setStyle("-fx-font-weight: bold; -fx-text-fill: #1877f2;"));
        name.setOnMouseExited(e -> name.setStyle("-fx-font-weight: bold; -fx-text-fill: #111;"));
        name.setOnMouseClicked(e -> Navigator.goToUserProfile(userId));
        return name;
    }

    private boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }

    @FXML private void goToFriends() {

        openFriendsModal();
    }
    @FXML
    private void onSearch() {
        if (Session.getCurrentUser() == null) return;

        String q = (searchField.getText() == null) ? "" : searchField.getText().trim();
        if (q.isEmpty()) return;

        openSearchModal(q);
    }
    @FXML private void goToChat() {

        System.out.println("Chat clicked");
    }
    @FXML private void onLogout() {
        authService.logout();
        Navigator.goToLogin();
    }
@FXML private void onRefresh() {
        page = 0;
        hasMore = true;
        isLoading = false;
        postsContainer.getChildren().clear();
        feedScroll.setVvalue(0);
        loadNextPage();
    }

    @FXML
    private void onProfile() {
            int userId = com.socialmedia.utils.Session.getCurrentUser().getId();
            Navigator.goToProfile(userId);

    }

    private void loadNotificationCount() {
        new Thread(() -> {
            try {
                int userId = Session.getCurrentUser().getId();
                int unreadCount = notificationService.getUnreadCount(userId);
                Platform.runLater(() -> {
                    notificationBadge.setText(String.valueOf(unreadCount));
                    if (unreadCount > 0) {
                        notificationBadge.setVisible(true);
                    } else {
                        notificationBadge.setVisible(false);
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void onNotifications() {
        openNotificationsMenu();
    }
    
    private void openNotificationsMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-font-size: 11px; -fx-padding: 0;");
        
        // Show loading item first
        MenuItem loadingItem = new MenuItem("Loading...");
        loadingItem.setDisable(true);
        menu.getItems().add(loadingItem);
        
        // Show menu immediately
        menu.show(notificationBtn, Side.BOTTOM, 10, 0);
        
        // Load notifications in background
        new Thread(() -> {
            try {
                int userId = Session.getCurrentUser().getId();
                java.util.List<Notification> notifications = notificationService.getAllNotifications(userId);
                
                System.out.println("[DEBUG] Loaded " + notifications.size() + " notifications");
                
                Platform.runLater(() -> {
                    menu.getItems().clear();
                    
                    if (notifications.isEmpty()) {
                        Label emptyLabel = new Label("No notifications");
                        emptyLabel.setStyle("-fx-text-fill: #65676b; -fx-padding: 10;");
                        CustomMenuItem emptyItem = new CustomMenuItem(emptyLabel);
                        emptyItem.setHideOnClick(false);
                        menu.getItems().add(emptyItem);
                    } else {
                        for (int i = 0; i < notifications.size(); i++) {
                            Notification notif = notifications.get(i);
                            System.out.println("[DEBUG] Adding notification " + (i + 1) + ": " + notif.getType());
                            CustomMenuItem item = createNotificationMenuItem(notif, menu);
                            menu.getItems().add(item);
                            
                            // Add separator between items (except after last item)
                            if (i < notifications.size() - 1) {
                                menu.getItems().add(new SeparatorMenuItem());
                            }
                        }
                    }
                    // Refresh menu size
                    menu.getScene().getWindow().sizeToScene();
                });
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("[ERROR] Loading notifications failed: " + e.getMessage());
                Platform.runLater(() -> {
                    menu.getItems().clear();
                    Label errorLabel = new Label("Failed to load notifications");
                    errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-padding: 10;");
                    CustomMenuItem errorItem = new CustomMenuItem(errorLabel);
                    errorItem.setHideOnClick(false);
                    menu.getItems().add(errorItem);
                });
            }
        }).start();
    }
    
    private CustomMenuItem createNotificationMenuItem(Notification notif, ContextMenu menu) {
        VBox content = new VBox(2);
        content.setPrefWidth(300);
        content.setPadding(new Insets(8));
        content.setStyle("-fx-background-color: " + (notif.isIsRead() ? "#ffffff" : "#e7f3ff") + ";");
        
        Label typeLabel = new Label(getNotificationLabel(notif));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #1877f2;");
        typeLabel.setWrapText(true);
        
        Label timeLabel = new Label(com.socialmedia.utils.TimeAgo.from(notif.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill: #65676b; -fx-font-size: 10;");
        
        content.getChildren().addAll(typeLabel, timeLabel);
        
        CustomMenuItem item = new CustomMenuItem(content);
        item.setHideOnClick(false);
        
        content.setOnMouseClicked(e -> {
            try {
                notificationService.markAsRead(notif.getId());
                loadNotificationCount();
                menu.hide();
                // Navigate based on notification type
                navigateFromNotification(notif);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        
        content.setOnMouseEntered(e -> content.setStyle("-fx-background-color: #f0f2f5;"));
        content.setOnMouseExited(e -> content.setStyle("-fx-background-color: " + (notif.isIsRead() ? "#ffffff" : "#e7f3ff") + ";"));
        content.setCursor(javafx.scene.Cursor.HAND);
        
        return item;
    }
    
    private String getNotificationLabel(Notification notif) {
        String type = notif.getType().toLowerCase();
        if (type.contains("liked")) {
            return "👍 Someone liked your post";
        } else if (type.contains("commented")) {
            return "💬 Someone commented on your post";
        } else if (type.contains("friend")) {
            return "👥 New friend request";
        } else if (type.contains("accepted")) {
            return "✅ Friend request accepted";
        } else {
            return notif.getType();
        }
    }
    
    private void navigateFromNotification(Notification notif) {
        String type = notif.getType().toLowerCase();
        
        if (type.contains("friend") || type.contains("accepted")) {
            // Navigate to the sender's profile
            Navigator.goToUserProfile(notif.getSenderId());
        } else if (type.contains("liked") || type.contains("commented")) {
            // Navigate to the post (reference_id is the post id)
            // For now, refresh the feed - ideally we'd scroll to the specific post
            onRefresh();
            System.out.println("[Navigation] Post ID: " + notif.getReferenceId());
        } else {
            System.out.println("[Navigation] Unknown notification type: " + notif.getType());
        }
    }
}