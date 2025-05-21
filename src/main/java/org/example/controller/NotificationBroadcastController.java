package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.example.entity.Notification;
import org.example.entity.User;
import org.example.repository.NotificationRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationBroadcastController {

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User currentAdmin;

    @FXML
    public void initialize() {
        statusLabel.setText("");
    }

    @FXML
    public void sendNotification() {
        String message = messageTextArea.getText().trim();
        if (message.isEmpty()) {
            statusLabel.setText("Ошибка: текст уведомления не может быть пустым.");
            statusLabel.setStyle("-fx-text-fill: #F44336;");
            return;
        }

        try {
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                statusLabel.setText("Нет пользователей для рассылки.");
                statusLabel.setStyle("-fx-text-fill: #F44336;");
                return;
            }

            for (User user : users) {
                Notification notification = Notification.builder()
                        .userId(Long.valueOf(user.getUserId()))
                        .content(message)
                        .timestamp(LocalDateTime.now())
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
            }

            statusLabel.setText("Уведомление успешно отправлено " + users.size() + " пользователям.");
            statusLabel.setStyle("-fx-text-fill: #4CAF50;");
            messageTextArea.clear();
        } catch (Exception e) {
            statusLabel.setText("Ошибка при отправке уведомления: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #F44336;");
            e.printStackTrace();
        }
    }

    public void setCurrentAdmin(User admin) {
        this.currentAdmin = admin;
    }
}