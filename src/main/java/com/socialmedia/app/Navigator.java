package com.socialmedia.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class Navigator {
    private static Stage stage;

    private Navigator() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void goTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(Navigator.class.getResource(fxmlPath));
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }

    public static void goToLogin() {
        goTo("/ui/views/login.fxml", "Login");
    }

    public static void goToRegister() {
        goTo("/ui/views/register.fxml", "Register");
    }

    public static void goToFeed() {
        goTo("/ui/views/feed.fxml", "News Feed");
    }

    public static void goToProfile(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/ui/views/profile.fxml"));
            Parent root = loader.load();
            
            com.socialmedia.ui.controllers.ProfileController controller = loader.getController();
            controller.loadUserProfile(userId);
            
            Scene scene = new Scene(root);
            stage.setTitle("Profile");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load profile view: " + e.getMessage(), e);
        }
    }

    public static void goToProfileEdit(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/ui/views/profile-edit.fxml"));
            Parent root = loader.load();
            
            com.socialmedia.ui.controllers.ProfileController controller = loader.getController();
            controller.loadUserProfile(userId);
            
            Scene scene = new Scene(root);
            stage.setTitle("Edit Profile");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load profile edit view: " + e.getMessage(), e);
        }
    }
}