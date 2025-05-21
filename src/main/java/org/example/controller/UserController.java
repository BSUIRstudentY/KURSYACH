package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.example.entity.User;
import org.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class UserController {

    @FXML
    private Label contentLabel;

    @FXML
    private Label notificationBadge;

    private User currentUser;

    private static ConfigurableApplicationContext springContext;

    @Autowired
    private NotificationService notificationService;

    private Timer badgeUpdateTimer;

    @FXML
    public void initialize() {
        contentLabel.setText(currentUser != null
                ? "Добро пожаловать, " + currentUser.getFirstName() + "!"
                : "Добро пожаловать в ваш аккаунт для торговли акциями!");
        updateNotificationBadge();
        badgeUpdateTimer = new Timer(true);
        badgeUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateNotificationBadge());
            }
        }, 0, 5000); // Обновление каждые 5 секунд
    }

    public void loadInitialContent() throws IOException {
        openProfile();
    }

    @FXML
    public void openProfile() throws IOException {
        loadContent("/profile.fxml", "Профиль");
    }

    @FXML
    public void openAssets() throws IOException {
        loadContent("/assets.fxml", "Активы");
    }

    @FXML
    public void openSubscriptions() throws IOException {
        loadContent("/subscriptions.fxml", "Подписки");
    }

    @FXML
    public void openTransactions() throws IOException {
        loadContent("/transactions.fxml", "Пополнение/Снятие");
    }

    @FXML
    public void openExchange() throws IOException {
        loadContent("/exchange.fxml", "Обмен валюты");
    }

    @FXML
    public void openNotifications() throws IOException {
        loadContent("/notifications.fxml", "Уведомления");
    }

    @FXML
    public void openAccounts() throws IOException {
        loadContent("/accounts.fxml", "Счета");
    }

    @FXML
    public void openTransactionHistory() throws IOException {
        loadContent("/transactionHistory.fxml", "История операций");
    }

    @FXML
    public void openSupport() throws IOException {
        loadContent("/support.fxml", "Поддержка");
    }

    @FXML
    public void logout() throws IOException {
        currentUser = null;
        loadContent("/login.fxml", "Вход в систему торговли акциями");
    }

    private void loadContent(String fxmlPath, String title) throws IOException {
        System.out.println("Попытка загрузить: " + fxmlPath);
        java.net.URL location = getClass().getResource(fxmlPath);
        System.out.println("URL: " + location);
        if (location == null) {
            throw new IOException("Не найден " + fxmlPath + ". Убедитесь, что файл находится в src/main/resources");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setControllerFactory(springContext::getBean);
        System.out.println("Создание контроллера для: " + fxmlPath);

        Pane newContent;
        try {
            newContent = fxmlLoader.load();
            System.out.println("FXML успешно загружен: " + fxmlPath);
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        if (!(contentLabel.getScene().getRoot() instanceof javafx.scene.layout.BorderPane)) {
            throw new IllegalStateException("Корневой элемент сцены должен быть BorderPane");
        }
        ((javafx.scene.layout.BorderPane) contentLabel.getScene().getRoot()).setCenter(newContent);

        Object controller = fxmlLoader.getController();
        System.out.println("Контроллер загружен: " + (controller != null ? controller.getClass().getName() : "null"));
        if (controller instanceof AccountsController accountsController) {
            accountsController.setCurrentUser(currentUser);
        } else if (controller instanceof AssetsController assetsController) {
            assetsController.setCurrentUser(currentUser);
        } else if (controller instanceof ProfileController profileController) {
            profileController.setCurrentUser(currentUser);
        } else if (controller instanceof TransactionsController transactionsController) {
            transactionsController.setCurrentUser(currentUser);
        } else if (controller instanceof ExchangeController exchangeController) {
            exchangeController.setCurrentUser(currentUser);
        } else if (controller instanceof TransactionHistoryController transactionHistoryController) {
            transactionHistoryController.setCurrentUser(currentUser);
        } else if (controller instanceof SupportController supportController) {
            supportController.setCurrentUser(currentUser);
        } else if (controller instanceof NotificationsController notificationsController) {
            notificationsController.setCurrentUser(currentUser);
        } else if (controller instanceof SubscriptionsController subscriptionsController) {
            subscriptionsController.setCurrentUser(currentUser);
        }

        contentLabel.setText(title);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initialize();
    }

    public void updateNotificationBadge() {
        if (currentUser != null) {
            int unreadCount = notificationService.getUnreadNotificationsCount(Long.valueOf(currentUser.getUserId()));
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(unreadCount > 0);
        } else {
            notificationBadge.setText("0");
            notificationBadge.setVisible(false);
        }
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }

    public void shutdown() {
        if (badgeUpdateTimer != null) {
            badgeUpdateTimer.cancel();
        }
    }
}