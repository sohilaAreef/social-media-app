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
        try {
            User user = AuthService.login(emailField.getText(), passwordField.getText());
            Session.setCurrentUser(user);

            Navigator.goToFeed();
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Something went wrong.");
        }
    }

    @FXML
    private void goToRegister() {
        Navigator.goToRegister();
    }
}