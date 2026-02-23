package com.socialmedia.app;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Navigator.init(stage);
        Navigator.goToLogin();
    }

    public static void main(String[] args) {
        launch();
    }
}

