package com.socialmedia.ui.controllers;

import com.socialmedia.app.Navigator;
import com.socialmedia.services.AuthService;
import javafx.fxml.FXML;

public class FeedController {

    @FXML
    private void onLogout() {
        new AuthService().logout();
        Navigator.goToLogin();
    }
}