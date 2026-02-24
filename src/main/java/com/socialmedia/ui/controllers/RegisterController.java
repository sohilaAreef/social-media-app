package com.socialmedia.ui.controllers;

import com.socialmedia.app.Navigator;
import com.socialmedia.models.User;
import com.socialmedia.services.AuthService;
import com.socialmedia.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void onRegister() {
        errorLabel.setText("");

        String name = nameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();
        String confirm = confirmField.getText();

        if (name == null || name.isBlank() || email == null || email.isBlank() || pass == null || pass.isBlank()) {
            errorLabel.setText("Please fill all fields");
            return;
        }
        if (!pass.equals(confirm)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        try {
            User user = authService.register(name, email, pass);

            Session.setCurrentUser(user);
            Navigator.goToFeed();

        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Something went wrong. Please try again.");
        }
    }

    @FXML
    private void goToLogin() {
        Navigator.goToLogin();
    }
}