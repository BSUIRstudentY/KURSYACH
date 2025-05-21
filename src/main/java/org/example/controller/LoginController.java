package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.entity.User;
import org.example.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label errorLabel;

    @Autowired
    private AuthService authService;

    private static ConfigurableApplicationContext springContext;

    @FXML
    public void handleLogin() throws IOException {
        User user = authService.authenticate(emailField.getText(), passwordField.getText());
        String fxmlPath = user.getRole() == User.Role.ADMIN ? "/admin.fxml" : "/user.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
        fxmlLoader.setControllerFactory(c -> springContext.getBean(c));

        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setScene(scene);
        stage.setTitle(user.getRole() == User.Role.ADMIN ? "Админ-панель" : "Панель пользователя");

        Object controller = fxmlLoader.getController();
        if (controller instanceof UserController userController) {
            userController.setCurrentUser(user);
            stage.show(); // Показываем сцену
            userController.loadInitialContent(); // Загружаем начальное содержимое после отображения сцены
        } else if (controller instanceof AdminController adminController) {
            adminController.setCurrentUser(user);
            stage.show();
        }
    }

    @FXML
    public void goToRegister() throws IOException {
        java.net.URL location = getClass().getResource("/register.fxml");
        if (location == null) {
            throw new IOException("Не найден register.fxml");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(location);
        fxmlLoader.setControllerFactory(c -> springContext.getBean(c));

        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.setScene(new Scene(fxmlLoader.load(), 400, 400));
        stage.setTitle("Регистрация в системе торговли акциями");
        stage.show();
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}