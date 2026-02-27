package com.socialmedia.ui.controllers;

import java.sql.SQLException;
import java.util.List;
import com.socialmedia.app.Navigator;
import com.socialmedia.services.AuthService;
import com.socialmedia.services.LikeService;
import com.socialmedia.services.PostService;
import com.socialmedia.services.CommentService;
import com.socialmedia.models.Comment;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import com.socialmedia.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.socialmedia.models.FeedPost;
import com.socialmedia.services.FeedService;
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

    @FXML private void goToFriends() {

        System.out.println("Chat clicked");
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