package com.socialmedia.ui.controllers;

import com.socialmedia.Enums.FriendStatus;
import com.socialmedia.app.Navigator;
import com.socialmedia.dao.PostDao;
import com.socialmedia.dao.ProfileDao;
import com.socialmedia.dao.UserDao;
import com.socialmedia.models.FeedPost;
import com.socialmedia.models.Profile;
import com.socialmedia.services.FriendService;
import com.socialmedia.utils.Session;
import com.socialmedia.utils.TimeAgo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Optional;

public class UserProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label nameLabel;
    @FXML private Label bioLabel;
    @FXML private Label statusLabel;
    @FXML private ScrollPane profileScroll;
    @FXML private VBox postsContainer;
    @FXML private Button friendBtn;
    @FXML private HBox incomingBox;

    private final FriendService friendService = new FriendService();
    private final ProfileDao profileDao = new ProfileDao();
    private final UserDao userDao = new UserDao();
    private final PostDao postDao = new PostDao();

    private int currentUserId;
    private int viewedUserId;
    private FriendStatus status;
    private int page = 0;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private boolean isFriend = false;

    @FXML
    public void initialize() {
        if (Session.getCurrentUser() == null) {
            Navigator.goToLogin();
            return;
        }

        currentUserId = Session.getCurrentUser().getId();
        Integer vId = Session.getViewedUserId();
        if (vId == null) {
            Navigator.goToFeed();
            return;
        }
        viewedUserId = vId;

        loadUserProfile();
        refreshFriendUI();
        initProfileFeed();
    }

    private void initProfileFeed() {
        try {
            // compute relation once
            isFriend = friendService.isFriends(currentUserId, viewedUserId);
        } catch (SQLException e) {
            e.printStackTrace();
            isFriend = false;
        }

        page = 0;
        hasMore = true;
        isLoading = false;
        postsContainer.getChildren().clear();

        loadNextProfilePage();

        // lazy loading
        if (profileScroll != null) {
            profileScroll.vvalueProperty().addListener((obs, ov, nv) -> {
                if (!hasMore || isLoading) return;
                if (nv.doubleValue() > 0.92) loadNextProfilePage();
            });
        }
    }

    private void loadNextProfilePage() {
        if (isLoading || !hasMore) return;
        isLoading = true;

        int offset = page * PAGE_SIZE;

        new Thread(() -> {
            try {
                var posts = postDao.getUserPostsForViewer(
                        viewedUserId,
                        currentUserId,
                        isFriend,
                        PAGE_SIZE,
                        offset
                );

                Platform.runLater(() -> {
                    for (var p : posts) {
                        postsContainer.getChildren().add(createPostCard(p));
                    }
                    if (posts.size() < PAGE_SIZE) hasMore = false;
                    page++;
                    isLoading = false;
                });

            } catch (SQLException e) {
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
        
        return card;
    }

    private void loadUserProfile() {
        try {
            // name
            var user = userDao.findById(viewedUserId);
            nameLabel.setText(user != null ? user.getName() : ("User #" + viewedUserId));

            // bio + img from profile
            Profile p = profileDao.getByUserId(viewedUserId);
            bioLabel.setText((p.getBio() == null || p.getBio().isBlank()) ? "No bio yet." : p.getBio());

            if (p.getImg() != null && !p.getImg().isBlank()) {
                profileImageView.setImage(new Image("file:" + p.getImg(), true));
            } else {
                profileImageView.setImage(null);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshFriendUI() {
        try {
            if (viewedUserId == currentUserId) {
                friendBtn.setVisible(false);
                friendBtn.setManaged(false);
                incomingBox.setVisible(false);
                incomingBox.setManaged(false);
                statusLabel.setText("");
                return;
            }

            status = friendService.getStatus(currentUserId, viewedUserId);

            // default hide incoming actions
            incomingBox.setVisible(false);
            incomingBox.setManaged(false);

            switch (status) {
                case NONE -> {
                    friendBtn.setText("Add Friend");
                    friendBtn.setStyle("-fx-background-color: #e4e6eb; -fx-font-weight: bold; -fx-background-radius: 10;");
                    statusLabel.setText("");
                }
                case OUTGOING_PENDING -> {
                    friendBtn.setText("Pending");
                    friendBtn.setStyle("-fx-background-color: #e4e6eb; -fx-font-weight: bold; -fx-background-radius: 10;");
                    statusLabel.setText("Friend request sent");
                }
                case FRIENDS -> {
                    friendBtn.setText("Friends");
                    friendBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
                    statusLabel.setText("");
                }
                case INCOMING_PENDING -> {
                    friendBtn.setText("Pending");
                    friendBtn.setStyle("-fx-background-color: #e4e6eb; -fx-font-weight: bold; -fx-background-radius: 10;");
                    statusLabel.setText("This user sent you a request");
                    incomingBox.setVisible(true);
                    incomingBox.setManaged(true);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onFriendAction() {
        try {
            if (status == FriendStatus.NONE) {
                friendService.addFriend(currentUserId, viewedUserId);
            } else if (status == FriendStatus.OUTGOING_PENDING) {
                if (confirm("Cancel friend request?", "Do you want to remove your friend request?")) {
                    friendService.cancelPending(currentUserId, viewedUserId);
                }
            } else if (status == FriendStatus.FRIENDS) {
                if (confirm("Remove friend?", "Do you want to remove this friend?")) {
                    friendService.unfriend(currentUserId, viewedUserId);
                }
            }
            // INCOMING_PENDING handled by accept/decline buttons
            refreshFriendUI();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAccept() {
        try {
            friendService.acceptIncoming(currentUserId, viewedUserId);
            refreshFriendUI();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDecline() {
        try {
            if (confirm("Decline request?", "Do you want to decline this friend request?")) {
                friendService.declineIncoming(currentUserId, viewedUserId);
                refreshFriendUI();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBack() {
        Navigator.goToFeed();
    }

    private boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}