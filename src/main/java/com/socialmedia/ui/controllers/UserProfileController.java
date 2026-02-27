package com.socialmedia.ui.controllers;

import com.socialmedia.Enums.FriendStatus;
import com.socialmedia.app.Navigator;
import com.socialmedia.dao.ProfileDao;
import com.socialmedia.dao.UserDao;
import com.socialmedia.models.Profile;
import com.socialmedia.services.FriendService;
import com.socialmedia.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.Optional;

public class UserProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label nameLabel;
    @FXML private Label bioLabel;
    @FXML private Label statusLabel;

    @FXML private Button friendBtn;
    @FXML private HBox incomingBox;

    private final FriendService friendService = new FriendService();
    private final ProfileDao profileDao = new ProfileDao();
    private final UserDao userDao = new UserDao();

    private int currentUserId;
    private int viewedUserId;
    private FriendStatus status;

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