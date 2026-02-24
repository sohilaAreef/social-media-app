package com.socialmedia.ui.controllers;

import java.sql.SQLException;

import com.socialmedia.app.Navigator;
import com.socialmedia.services.AuthService;
import com.socialmedia.services.PostService;
import com.socialmedia.utils.Session;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class FeedController {

    @FXML 
    private TextArea postContentArea; 
    
    private final PostService postService = new PostService();
    private final AuthService authService = new AuthService();

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

    @FXML
    private void onLogout() {
        authService.logout();
        Navigator.goToLogin();
    }
}