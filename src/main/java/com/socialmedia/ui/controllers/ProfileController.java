package com.socialmedia.ui.controllers;

import com.socialmedia.app.Navigator;
import com.socialmedia.models.Profile;
import com.socialmedia.models.User;
import com.socialmedia.models.UserProfile;
import com.socialmedia.services.ProfileService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfileController {

    private final ProfileService profileService = new ProfileService();
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

    @FXML private TextField txtName;
    @FXML private PasswordField txtPassword;

    
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

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}