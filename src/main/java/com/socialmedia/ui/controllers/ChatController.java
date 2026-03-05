package com.socialmedia.ui.controllers;

import com.socialmedia.models.Message;
import com.socialmedia.services.ChatService;
import com.socialmedia.utils.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChatController {

    private static final ConcurrentHashMap<Integer, ChatController> OPEN_CHATS = new ConcurrentHashMap<>();
    @FXML private Label lblName;
    @FXML private VBox messagesBox;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField txtMessage;

    private int otherUserId;

    private final ChatService chatService = new ChatService();

    public void initChat(int userId, String name) {
        this.otherUserId = userId;
        lblName.setText(name);

        OPEN_CHATS.put(otherUserId, this);


        if (Session.getChatClient() != null) {
            Session.getChatClient().setOnIncoming((senderId, text) -> {
                ChatController chat = OPEN_CHATS.get(senderId);
                if (chat != null) {
                    Platform.runLater(() -> chat.appendIncoming(senderId, text));
                } else {

                    System.out.println("New message from " + senderId + ": " + text);
                }
            });
        }

        loadMessages();
    }
    public void appendIncoming(int senderId, String text) {
        Message m = new Message();
        m.setSenderId(senderId);
        m.setReceiverId(Session.getCurrentUser().getId());
        m.setContent(text);

        messagesBox.getChildren().add(createMessageBubble(m));
        scrollPane.setVvalue(1);
    }
    private void loadMessages() {
        try {
            int myId = Session.getCurrentUser().getId();

            List<Message> msgs = chatService.loadConversation(myId, otherUserId);

            messagesBox.getChildren().clear();

            for (Message m : msgs) {
                messagesBox.getChildren().add(createMessageBubble(m));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createMessageBubble(Message m) {

        int myId = Session.getCurrentUser().getId();

        Label text = new Label(m.getContent());
        text.setWrapText(true);
        text.setPadding(new Insets(8));

        HBox row = new HBox();

        if (m.getSenderId() == myId) {

            text.setStyle("""
                    -fx-background-color: #1877f2;
                    -fx-text-fill: white;
                    -fx-background-radius: 10;
            """);

            row.setStyle("-fx-alignment: center-right;");

        } else {

            text.setStyle("""
                    -fx-background-color: #e4e6eb;
                    -fx-background-radius: 10;
            """);

            row.setStyle("-fx-alignment: center-left;");
        }

        row.getChildren().add(text);
        return row;
    }

    @FXML
    private void onSend() {
        String text = txtMessage.getText();
        if (text == null || text.trim().isEmpty()) return;

        int myId = Session.getCurrentUser().getId();

        try {
            Message m = chatService.send(myId, otherUserId, text);

            if (Session.getChatClient() != null) {
                Session.getChatClient().send(otherUserId, text.trim());
            }

            messagesBox.getChildren().add(createMessageBubble(m));
            txtMessage.clear();
            scrollPane.setVvalue(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onClose() {
        OPEN_CHATS.remove(otherUserId);
        Stage stage = (Stage) txtMessage.getScene().getWindow();
        stage.close();
    }
}