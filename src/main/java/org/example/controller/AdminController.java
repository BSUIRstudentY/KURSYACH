package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.example.entity.User;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AdminController {

    private static ConfigurableApplicationContext springContext;

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label contentLabel;

    private User currentUser;

    @FXML
    public void initialize() {
        if (mainPane == null || contentLabel == null) {
            System.err.println("FXML-поля в AdminController не инициализированы!");
            System.err.println("mainPane: " + mainPane);
            System.err.println("contentLabel: " + contentLabel);
            throw new IllegalStateException("Не удалось инициализировать FXML-поля в AdminController");
        }

        contentLabel.setText(currentUser != null
                ? "Добро пожаловать, " + currentUser.getFirstName() + " (Админ)!"
                : "Управление пользователями и ценными бумагами");
        try {
            loadInitialContent();
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке начального контента: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadInitialContent() throws IOException {
        openUserManagement();
    }

    @FXML
    public void openUserManagement() throws IOException {
        loadContent("/userManagement.fxml", "Управление пользователями");
    }

    @FXML
    public void openCommissionManagement() throws IOException {
        loadContent("/commissionManagement.fxml", "Управление комиссиями");
    }

    @FXML
    public void openAssetManagement() throws IOException {
        loadContent("/assetManagement.fxml", "Управление активами");
    }

    @FXML
    public void openAccountManagement() throws IOException {
        loadContent("/accountManagement.fxml", "Управление счетами");
    }

    @FXML
    public void openStatistics() throws IOException {
        loadContent("/statistics.fxml", "Статистика");
    }

    @FXML
    public void openNotificationBroadcast() throws IOException {
        loadContent("/notificationBroadcast.fxml", "Рассылка уведомлений");
    }

    @FXML
    public void openTransactionLogs() throws IOException {
        loadContent("/transactionLogs.fxml", "Логи транзакций");
    }

    @FXML
    public void openSupport() throws IOException {
        loadContent("/adminSupport.fxml", "Поддержка (Админ)");
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

        Parent newContent;
        try {
            newContent = fxmlLoader.load();
            System.out.println("FXML успешно загружен: " + fxmlPath);
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        mainPane.setCenter(newContent);
        contentLabel.setText(title);

        Object controller = fxmlLoader.getController();
        if (controller instanceof AdminSupportController adminSupportController) {
            adminSupportController.setCurrentAdmin(currentUser);
        } else if (controller instanceof TransactionLogsController transactionLogsController) {
            transactionLogsController.setCurrentAdmin(currentUser);
        } else if (controller instanceof NotificationBroadcastController notificationBroadcastController) {
            notificationBroadcastController.setCurrentAdmin(currentUser);
        }
        System.out.println("Контроллер загружен: " + (controller != null ? controller.getClass().getName() : "null"));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initialize();
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
        if (springContext == null) {
            System.err.println("Spring Context не установлен!");
        } else {
            System.out.println("Spring Context успешно установлен.");
        }
    }
}