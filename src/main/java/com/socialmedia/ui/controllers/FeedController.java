package com.socialmedia.ui.controllers;

import java.sql.SQLException;
import java.util.List;

import com.socialmedia.app.Navigator;
import com.socialmedia.services.AuthService;
import com.socialmedia.services.PostService;
import com.socialmedia.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import com.socialmedia.models.FeedPost;
import com.socialmedia.services.FeedService;
import com.socialmedia.utils.TimeAgo;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
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

    private final FeedService feedService = new FeedService();
    private final PostService postService = new PostService();
    private final AuthService authService = new AuthService();

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
            } else {
                System.err.println("Error: Session is null. Please login first.");
            }

        } catch (SQLException e) {
            System.err.println("Error: Failed to publish post: " + e.getMessage());
        }
    }

    private void resetAndReloadFeed() {
        page = 0;
        hasMore = true;
        postsContainer.getChildren().clear();
        loadNextPage();
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

        Button likeBtn = new Button("Like");
        likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-weight: bold;");
        likeBtn.setOnAction(e -> onLike(post.getPostId()));

        Button commentBtn = new Button("Comment");
        commentBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-weight: bold;");
        commentBtn.setOnAction(e -> onComment(post.getPostId()));

        actions.getChildren().addAll(likeBtn, commentBtn);
        card.getChildren().add(actions);

        return card;
    }

    private void onLike(int postId) {
        System.out.println("Like clicked for postId=" + postId);
    }

    private void onComment(int postId) {
        System.out.println("Comment clicked for postId=" + postId);
    }

    @FXML private void goToProfile() {

        System.out.println("Chat clicked");
    }
    @FXML private void goToFriends() {

        System.out.println("Chat clicked");
    }
    @FXML private void goToSearch() {

        System.out.println("Chat clicked");
    }
    @FXML private void goToChat() {

        System.out.println("Chat clicked");
    }
    @FXML private void goToNotifications() {

        System.out.println("Chat clicked");
    }
    @FXML private void onLogout() {
        authService.logout();
        Navigator.goToLogin();
    }
}