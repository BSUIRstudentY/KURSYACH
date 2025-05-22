package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.controller.*;
import org.example.service.AccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class KURSYACHApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = SpringApplication.run(KURSYACHApplication.class);
        springContext.getBean(AccountsController.class);
        springContext.getBean(AccountService.class);
        AdminController.setSpringContext(springContext);
    }

    @Override
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Произошла непредвиденная ошибка: " + throwable.getMessage());
            throwable.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Произошла ошибка");
                alert.setContentText(throwable.getMessage());
                alert.showAndWait();
            });
        });

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
            fxmlLoader.setControllerFactory(springContext::getBean);
            Scene scene = new Scene(fxmlLoader.load(), 400, 300);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Вход в систему торговли акциями");
            primaryStage.show();

            LoginController.setSpringContext(springContext);
            UserController.setSpringContext(springContext);
            RegisterController.setSpringContext(springContext);
            AccountsController.setSpringContext(springContext);
            AssetsController.setSpringContext(springContext);
            AdminController.setSpringContext(springContext);
            ProfileController.setSpringContext(springContext);
            TransactionsController.setSpringContext(springContext);
            ExchangeController.setSpringContext(springContext);
            NotificationsController.setSpringContext(springContext);
            SubscriptionsController.setSpringContext(springContext);
            SupportController.setSpringContext(springContext);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось запустить приложение");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            throw new RuntimeException("Не удалось запустить приложение: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        springContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}