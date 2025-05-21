package org.example.controller;

import javafx.collections.FXCollections;
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
public class RegisterController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField middleNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    @FXML
    private Button registerButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private Label errorLabel;

    @Autowired
    private AuthService authService;

    private static ConfigurableApplicationContext springContext;

    @FXML
    public void initialize() {
        roleChoiceBox.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
        roleChoiceBox.setValue("USER");
    }

    @FXML
    public void handleRegister() throws IOException {
        try {
            String firstName = firstNameField.getText();
            String middleName = middleNameField.getText().isEmpty() ? null : middleNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String roleValue = roleChoiceBox.getValue();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || roleValue == null) {
                errorLabel.setText("Все обязательные поля должны быть заполнены, включая роль");
                return;
            }

            User.Role role;
            try {
                role = User.Role.valueOf(roleValue.toUpperCase());
            } catch (IllegalArgumentException e) {
                errorLabel.setText("Недопустимая роль: " + roleValue + ". Доступны USER или ADMIN.");
                return;
            }

            authService.register(firstName, middleName, lastName, email, password, role);
            goToLogin();
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Ошибка регистрации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goToLogin() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
        fxmlLoader.setControllerFactory(c -> springContext.getBean(c));

        Stage stage = (Stage) backToLoginButton.getScene().getWindow();
        stage.setScene(new Scene(fxmlLoader.load(), 400, 300));
        stage.setTitle("Вход в систему торговли акциями");
        stage.show();
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}