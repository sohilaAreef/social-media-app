package com.socialmedia.ui.controllers;

import java.sql.SQLException;
import java.util.List;
import com.socialmedia.app.Navigator;
import com.socialmedia.dao.FriendDao;
import com.socialmedia.services.*;
import com.socialmedia.models.Comment;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import com.socialmedia.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    private final FeedService feedService = new FeedService();
    private final PostService postService = new PostService();
    private final AuthService authService = new AuthService();
    private final LikeService likeService = new LikeService();
    private final CommentService commentService = new CommentService();
    private final FriendService friendService = new FriendService();

    private int page = 0;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMore = true;

    @FXML
    public void initialize() {
        loadNextPage();

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
                List<FeedPost> posts = feedService.loadPage(page, pageSize);

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

            row.getChildren().addAll(name, spacer, view);
            box.getChildren().add(row);
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
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isEmpty();
    }
    
    @FXML private void goToFriends() {

        openFriendsModal();
    }
    @FXML private void onSearch() {
        String q = (searchField.getText() == null) ? "" : searchField.getText().trim();
        if (q.isEmpty()) return;

        System.out.println("Search query: " + q);
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
}