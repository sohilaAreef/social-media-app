package com.socialmedia.ui.controllers;

import com.socialmedia.app.Navigator;
import com.socialmedia.dao.ProfileDao.ProfileData;
import com.socialmedia.models.Profile;
import com.socialmedia.services.ProfileService;
import com.socialmedia.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.UUID;

public class ProfileController {

    @FXML private ImageView profileImageView;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private TextArea bioArea;
    @FXML private Label statusLabel;
    @FXML private Label photoPathLabel;

    private final ProfileService profileService = new ProfileService();

    private String selectedImagePath = null; // path we will save in DB

    @FXML
    public void initialize() {
        if (Session.getCurrentUser() == null) {
            Navigator.goToLogin();
            return;
        }

        nameLabel.setText(Session.getCurrentUser().getName());
        emailLabel.setText(Session.getCurrentUser().getEmail());

        loadProfileFromDb();
    }

    private void loadProfileFromDb() {
        try {
            int userId = Session.getCurrentUser().getId();
            Profile profile = profileService.loadProfile(userId);

            bioArea.setText(profile.getBio());
            selectedImagePath = profile.getImg();

            renderProfileImage(selectedImagePath);

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load profile.");
        }
    }

    private void renderProfileImage(String path) {
        try {
            if (path != null && !path.isBlank()) {
                Image img = new Image("file:" + path, true);
                profileImageView.setImage(img);
                photoPathLabel.setText(""); // keep UI clean
            } else {
                // optional default avatar from resources (if you add it)
                //Image img = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
                //profileImageView.setImage(img);
                profileImageView.setImage(null);
                photoPathLabel.setText("No photo");
            }
        } catch (Exception ex) {
            profileImageView.setImage(null);
            photoPathLabel.setText("Invalid photo path");
        }
    }

    @FXML
    private void onUploadPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Profile Picture");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fc.showOpenDialog(profileImageView.getScene().getWindow());
        if (file == null) return;

        try {
            Path uploadsDir = Paths.get("uploads", "profiles");
            Files.createDirectories(uploadsDir);

            String ext = getFileExtension(file.getName());
            String newName = "profile_" + Session.getCurrentUser().getId() + "_" + UUID.randomUUID() + ext;

            Path dest = uploadsDir.resolve(newName);
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            selectedImagePath = dest.toAbsolutePath().toString();
            renderProfileImage(selectedImagePath);
            statusLabel.setText("Photo selected. Click Save.");

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to upload photo.");
        }
    }

    @FXML
    private void onSave() {
        try {
            int userId = Session.getCurrentUser().getId();
            String bio = bioArea.getText() == null ? "" : bioArea.getText().trim();

            profileService.updateProfile(userId, selectedImagePath, bio);
            statusLabel.setText("Saved ✅");

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to save.");
        }
    }

    @FXML
    private void onBack() {
        Navigator.goToFeed();
    }

    private String getFileExtension(String name) {
        int idx = name.lastIndexOf('.');
        if (idx == -1) return "";
        return name.substring(idx).toLowerCase();
    }
}