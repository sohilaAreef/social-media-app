package com.socialmedia.app;

import com.socialmedia.utils.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public final class Navigator {
    private static Stage stage;

    private Navigator() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void goTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(Navigator.class.getResource(fxmlPath)));
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

    public static void goToProfile() { goTo("/ui/views/profile.fxml", "Profile");}

    public static void goToUserProfile(int userId) {
        Session.setViewedUserId(userId);
        goTo("/ui/views/user_profile.fxml" , "User Profile");}
}