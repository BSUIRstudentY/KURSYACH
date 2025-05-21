package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import org.example.entity.Notification;
import org.example.entity.User;
import org.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class NotificationsController {
    private static ConfigurableApplicationContext springContext;

    @FXML
    private ListView<String> notificationList;

    @Autowired
    private NotificationService notificationService;

    private User currentUser;
    private Timer updateTimer;

    @FXML
    public void initialize() {
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateNotifications());
            }
        }, 0, 5000);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateNotifications();
    }

    private void updateNotifications() {
        if (currentUser == null) {
            notificationList.getItems().clear();
            notificationList.getItems().add("Пожалуйста, войдите в систему.");
            return;
        }

        List<Notification> notifications = notificationService.getNotificationsForUser(Long.valueOf(currentUser.getUserId()));
        notificationList.getItems().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        for (Notification notification : notifications) {
            String status = notification.isRead() ? "[Прочитано]" : "[Непрочитано]";
            String formattedTimestamp = notification.getTimestamp().format(formatter);
            notificationList.getItems().add(String.format("%s %s - %s", status, formattedTimestamp, notification.getContent()));
        }
        updateMainBadge();
    }

    @FXML
    public void markAllAsRead() {
        if (currentUser == null) {
            showAlert("Пожалуйста, войдите в систему.");
            return;
        }
        notificationService.markAllAsRead(Long.valueOf(currentUser.getUserId()));
        updateNotifications();
        showAlert("Все уведомления отмечены как прочитанные.");
    }

    private void updateMainBadge() {
        UserController userController = springContext.getBean(UserController.class);
        userController.updateNotificationBadge();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }

    public void shutdown() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }
}