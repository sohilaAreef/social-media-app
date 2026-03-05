package com.socialmedia.ui.controllers;

import com.socialmedia.app.Navigator;
import com.socialmedia.models.Profile;
import com.socialmedia.models.User;
import com.socialmedia.models.UserProfile;
import com.socialmedia.services.PostService;
import com.socialmedia.services.ProfileService;
import com.socialmedia.utils.TimeAgo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import com.socialmedia.models.FeedPost;
import com.socialmedia.services.FeedService;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.util.List;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfileController {

    private final ProfileService profileService = new ProfileService();
    private final PostService postService = new PostService();
    private int currentUserId;

    private File selectedImageFile;
    private String currentImagePath;

    @FXML private Label lblTitle, lblName, lblEmail, lblJoined;
    @FXML private TextArea lblBio,
            txtBio;
    @FXML private Button btnEdit,
            btnChangeImage, btnSave, btnCancel;
    @FXML private ImageView imgProfile;
    @FXML private VBox profileCard, emailSection, joinedSection;
    @FXML private HBox buttonBox;
    @FXML private VBox postsContainer;
    @FXML private TextField txtName;
    @FXML private PasswordField txtPassword;


    private final FeedService feedService = new FeedService();
    private int page = 0;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMore = true;

    public void loadUserProfile(int userId) {
        this.currentUserId = userId;

        UserProfile userProfile = profileService.getProfile(userId);
        if (userProfile == null) {
            return;
        }

        String name = userProfile.getName();
        String email = userProfile.getEmail();
        String bio = userProfile.getBio();
        Timestamp createdAt = userProfile.getCreatedAt();
        String imgPath = userProfile.getImg();

        // Store the current image path to preserve it if not changed
        this.currentImagePath = imgPath;
        this.selectedImageFile = null;  // Reset selected image file

        if (lblName != null) {
            lblName.setText(name != null ? name : "N/A");
        }
        if (lblEmail != null) {
            lblEmail.setText(email != null ? email : "N/A");
        }
        if (lblBio != null) {
            lblBio.setText(bio != null ? bio : "");
        }
        if (lblJoined != null && createdAt != null) {
            LocalDateTime localDateTime = createdAt.toLocalDateTime();
            String joinedDate = localDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
            lblJoined.setText(joinedDate);
        } else if (lblJoined != null) {
            lblJoined.setText("N/A");
        }


        if (txtName != null) {
            txtName.setText(name != null ? name : "");
        }
        if (txtBio != null) {
            txtBio.setText(bio != null ? bio : "");
        }


        Image image;
        try {
            if (imgPath != null && !imgPath.isBlank()) {
                image = new Image("file:" + imgPath, true);
            } else {
                image = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
            }
        } catch (Exception e) {
            image = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
        }

        if (imgProfile != null) {
            imgProfile.setImage(image);
        }
        loadMyPostsFirstPage();
    }

    private void loadMyPostsFirstPage() {
        if (postsContainer == null) return;

        page = 0;
        isLoading = false;
        hasMore = true;
        postsContainer.getChildren().clear();

        loadNextMyPostsPage();
    }

    private void loadNextMyPostsPage() {
        if (isLoading || !hasMore) return;
        isLoading = true;

        int offset = page * pageSize;

        new Thread(() -> {
            try {
                // Self => viewer = currentUserId, profileUserId = currentUserId
                List<FeedPost> posts = feedService.loadProfileFeedPage(currentUserId, currentUserId, page, pageSize);

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
        HBox actions = new HBox(10);

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("""
                        -fx-background-color: #ff4d4f;
                        -fx-text-fill: white;
                        -fx-font-weight: bold;
        """);

        deleteBtn.setOnAction(e -> onDeletePost(post.getPostId(), card));

        actions.getChildren().add(deleteBtn);

        card.getChildren().add(actions);

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

        //HBox actions = new HBox(10);
        actions.setPadding(new Insets(8, 0, 0, 0));
        actions.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        return card;
    }

    @FXML
    private void onEdit() {
        Navigator.goToProfileEdit(currentUserId);
    }

    @FXML
    public void onChangeImage() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );

            if (imgProfile.getScene() != null && imgProfile.getScene().getWindow() != null) {
                File file = fileChooser.showOpenDialog(imgProfile.getScene().getWindow());
                if (file != null) {
                    selectedImageFile = file;
                    imgProfile.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSaveProfile() {

        if (txtName == null || txtBio == null) {
            showAlert(Alert.AlertType.ERROR, "Error: Edit fields not found. Please reload the page.");
            return;
        }

        String name = txtName.getText().trim();
        String bio = txtBio.getText().trim();
        String newPassword = txtPassword != null ? txtPassword.getText().trim() : "";

        // Validate inputs
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Name cannot be empty!");
            return;
        }

        try {
            User user = new User(currentUserId, name, "");

            // Use the new image if selected, otherwise preserve the current image path
            String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : currentImagePath;
            Profile profile = new Profile(currentUserId, bio, imagePath);

            boolean success = profileService.updateProfile(user, profile, newPassword.isEmpty() ? null : newPassword);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Profile updated successfully!");
                if (txtPassword != null) {
                    txtPassword.clear();
                }
                selectedImageFile = null;

                // Navigate back to profile view
                Navigator.goToProfile(currentUserId);
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed to update profile. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    @FXML
    public void onCancelEdit() {
        Navigator.goToProfile(currentUserId);
    }

    @FXML
    private void onBack() {
        Navigator.goToFeed();
    }

    private void onDeletePost(int postId, VBox card) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this post?");

        alert.showAndWait().ifPresent(btn -> {

            if (btn == ButtonType.OK) {

                new Thread(() -> {
                    try {

                        boolean deleted = postService.deletePost(postId, currentUserId);

                        if (deleted) {
                            Platform.runLater(() ->
                                    postsContainer.getChildren().remove(card)
                            );
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}