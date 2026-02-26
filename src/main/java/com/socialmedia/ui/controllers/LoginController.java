package com.socialmedia.ui.controllers;

import com.socialmedia.models.User;
import com.socialmedia.services.AuthService;
import com.socialmedia.app.Navigator;
import com.socialmedia.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin() {
        errorLabel.setText("");

        String email = emailField.getText();
        String pass = passwordField.getText();

        AuthService authService = new AuthService();
        User user = authService.login(email, pass);
        
        if (user != null) {
            Session.setCurrentUser(user);
            Navigator.goToFeed();
        } else {
            errorLabel.setText("Login failed. Please try again.");
        }
    }

    @FXML
    private void goToRegister() {
        Navigator.goToRegister();
    }
}