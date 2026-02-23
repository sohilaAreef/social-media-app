package com.socialmedia.ui.controllers;

import com.socialmedia.app.Navigator;
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

    @FXML
    private void onRegister() {
        errorLabel.setText("");

        String name = nameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();
        String confirm = confirmField.getText();

        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            errorLabel.setText("Please fill all fields");
            return;
        }
        if (!pass.equals(confirm)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        // Mock: اعتبره اتسجل
        Navigator.goToLogin();
    }

    @FXML
    private void goToLogin() {
        Navigator.goToLogin();
    }
}