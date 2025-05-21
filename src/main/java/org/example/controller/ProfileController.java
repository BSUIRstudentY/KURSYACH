package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.entity.Account;
import org.example.entity.Asset;
import org.example.entity.Commission;
import org.example.entity.CommissionType;
import org.example.entity.Transaction;
import org.example.entity.User;
import org.example.repository.CommissionRepository;
import org.example.service.AssetService;
import org.example.service.TransactionService;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProfileController {
    private static ConfigurableApplicationContext springContext;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField middleNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField roleField;

    @FXML
    private TextField kycStatusField;

    @FXML
    private Button saveButton;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button portfolioButton;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private AssetService assetService;

    private User currentUser;

    @FXML
    public void initialize() {
        if (currentUser != null) {
            updateFields();
        }
    }

    private void updateFields() {
        firstNameField.setText(currentUser.getFirstName());
        middleNameField.setText(currentUser.getMiddleName() != null ? currentUser.getMiddleName() : "");
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
        roleField.setText(currentUser.getRole().toString());
        kycStatusField.setText(currentUser.getKycStatus().toString());
    }

    @FXML
    public void saveProfile() {
        try {
            String newFirstName = firstNameField.getText().trim();
            String newMiddleName = middleNameField.getText().trim();
            String newLastName = lastNameField.getText().trim();

            if (newFirstName.isEmpty() || newLastName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Имя и фамилия не могут быть пустыми!");
                return;
            }

            currentUser.setFirstName(newFirstName);
            currentUser.setMiddleName(newMiddleName.isEmpty() ? null : newMiddleName);
            currentUser.setLastName(newLastName);

            userService.updateUser(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Профиль успешно обновлен!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить профиль: " + e.getMessage());
        }
    }

    @FXML
    public void changePassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Смена пароля");
        dialog.setHeaderText("Введите новый пароль");

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        PasswordField currentPasswordField = new PasswordField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Текущий пароль:"),
                currentPasswordField,
                new Label("Новый пароль:"),
                newPasswordField,
                new Label("Подтвердите новый пароль:"),
                confirmPasswordField
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String currentPassword = currentPasswordField.getText();
                String newPassword = newPasswordField.getText();
                String confirmPassword = confirmPasswordField.getText();

                if (!currentPassword.equals(currentUser.getPassword())) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Текущий пароль неверный!");
                    return null;
                }
                if (!newPassword.equals(confirmPassword)) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Новый пароль и подтверждение не совпадают!");
                    return null;
                }
                if (newPassword.length() < 6) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Пароль должен быть не менее 6 символов!");
                    return null;
                }
                return newPassword;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                currentUser.setPassword(newPassword);
                userService.updateUser(currentUser);
                showAlert(Alert.AlertType.INFORMATION, "Успех", "Пароль успешно изменен!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось изменить пароль: " + e.getMessage());
            }
        });
    }

    @FXML
    public void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось выйти: " + e.getMessage());
        }
    }

    @FXML
    public void openPortfolio() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Мой портфель");
        dialog.setHeaderText("Ваши активы для продажи");

        ButtonType closeButtonType = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButtonType);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15;");

        try {
            Map<String, Double> portfolio = currentUser.getPortfolio() != null && !currentUser.getPortfolio().isEmpty()
                    ? objectMapper.readValue(currentUser.getPortfolio(), Map.class)
                    : new HashMap<>();

            if (portfolio.isEmpty()) {
                content.getChildren().add(new Label("Ваш портфель пуст."));
            } else {
                for (Map.Entry<String, Double> entry : portfolio.entrySet()) {
                    String ticker = entry.getKey();
                    double quantity = entry.getValue();

                    Asset asset = assetService.findByTicker(ticker).orElse(null);
                    if (asset == null) {
                        content.getChildren().add(new Label("Актив " + ticker + " не найден."));
                        continue;
                    }

                    HBox assetBox = new HBox(10);
                    Label assetLabel = new Label(String.format("%s - Количество: %.2f, Цена: %.2f %s", ticker, quantity, asset.getCurrentPrice(), asset.getCurrency()));
                    assetLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

                    Button sellButton = new Button("Продать");
                    sellButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
                    sellButton.setOnAction(e -> handleSellAsset(asset, quantity, dialog));

                    assetBox.getChildren().addAll(assetLabel, sellButton);
                    content.getChildren().add(assetBox);
                }
            }
        } catch (JsonProcessingException e) {
            content.getChildren().add(new Label("Ошибка загрузки портфеля: " + e.getMessage()));
        }

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void handleSellAsset(Asset asset, double quantity, Dialog<Void> dialog) {
        Dialog<Map<String, Object>> sellDialog = new Dialog<>();
        sellDialog.setTitle("Продажа актива: " + asset.getTicker());
        sellDialog.setHeaderText("Укажите количество для продажи и счет для зачисления");

        ButtonType sellButtonType = new ButtonType("Продать", ButtonBar.ButtonData.OK_DONE);
        sellDialog.getDialogPane().getButtonTypes().addAll(sellButtonType, ButtonType.CANCEL);

        TextField quantityField = new TextField();
        quantityField.setPromptText("Введите количество (до " + quantity + ")");
        quantityField.setStyle("-fx-pref-width: 200; -fx-background-color: #ffffff; -fx-border-radius: 5;");

        ComboBox<String> accountComboBox = new ComboBox<>();
        accountComboBox.getItems().addAll(currentUser.getAccounts().stream()
                .map(Account::getAccountNumber)
                .toList());
        accountComboBox.setPromptText("Выберите счет");
        accountComboBox.setStyle("-fx-pref-width: 200; -fx-background-color: #ffffff; -fx-border-radius: 5;");

        VBox content = new VBox(10);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.getChildren().addAll(
                new Label("Количество для продажи:"),
                quantityField,
                new Label("Счет для зачисления:"),
                accountComboBox
        );
        sellDialog.getDialogPane().setContent(content);

        sellDialog.setResultConverter(dialogButton -> {
            if (dialogButton == sellButtonType) {
                try {
                    double sellQuantity = Double.parseDouble(quantityField.getText());
                    if (sellQuantity <= 0 || sellQuantity > quantity) {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "Неверное количество для продажи!");
                        return null;
                    }
                    String selectedAccountNumber = accountComboBox.getValue();
                    if (selectedAccountNumber == null) {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "Выберите счет для зачисления!");
                        return null;
                    }
                    Map<String, Object> result = new HashMap<>();
                    result.put("quantity", sellQuantity);
                    result.put("accountNumber", selectedAccountNumber);
                    return result;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите корректное число!");
                    return null;
                }
            }
            return null;
        });

        sellDialog.showAndWait().ifPresent(result -> {
            double sellQuantity = (Double) result.get("quantity");
            String selectedAccountNumber = (String) result.get("accountNumber");
            BigDecimal totalAmount = BigDecimal.valueOf(sellQuantity)
                    .multiply(asset.getCurrentPrice())
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            // Получаем комиссию для продажи
            Commission commission = commissionRepository.findByCommissionType(CommissionType.ПРОДАЖА)
                    .orElse(Commission.builder()
                            .amount(BigDecimal.ZERO)
                            .currency(asset.getCurrency())
                            .commissionDate(LocalDateTime.now())
                            .commissionType(CommissionType.ПРОДАЖА)
                            .percentage(BigDecimal.ZERO)
                            .isActive(true)
                            .build());
            BigDecimal commissionAmount = commission.getAmount()
                    .add(totalAmount.multiply(commission.getPercentage().divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal netAmount = totalAmount.subtract(commissionAmount)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            // Находим счет для зачисления
            Account toAccount = currentUser.getAccounts().stream()
                    .filter(a -> a.getAccountNumber().equals(selectedAccountNumber))
                    .findFirst()
                    .orElse(null);
            if (toAccount == null) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Счет не найден!");
                return;
            }

            // Обновляем баланс счета
            try {
                Map<String, Double> balances = parseBalances(toAccount.getBalances());
                double currentBalance = balances.getOrDefault(asset.getCurrency(), 0.0);
                balances.put(asset.getCurrency(), currentBalance + netAmount.doubleValue());
                toAccount.setBalances(objectMapper.writeValueAsString(balances));

                // Обновляем портфель
                Map<String, Double> portfolio = objectMapper.readValue(currentUser.getPortfolio(), Map.class);
                double newQuantity = portfolio.get(asset.getTicker()) - sellQuantity;
                if (newQuantity <= 0) {
                    portfolio.remove(asset.getTicker());
                } else {
                    portfolio.put(asset.getTicker(), newQuantity);
                }
                currentUser.setPortfolio(objectMapper.writeValueAsString(portfolio));

                // Сохраняем изменения
                currentUser.getAccounts().forEach(account -> {
                    if (account.getAccountId().equals(toAccount.getAccountId())) {
                        account.setBalances(toAccount.getBalances());
                    }
                });
                userService.updateUser(currentUser);

                // Создаем транзакцию продажи
                Transaction transaction = Transaction.builder()
                        .user(currentUser)
                        .asset(asset)
                        .transactionType(Transaction.TransactionType.ПРОДАЖА)
                        .amount(netAmount)
                        .currency(asset.getCurrency())
                        .quantity(sellQuantity)
                        .transactionDate(LocalDateTime.now())
                        .toAccount(toAccount) // Счет зачисления
                        .build();
                transactionService.createTransaction(transaction);

                showAlert(Alert.AlertType.INFORMATION, "Успех", "Актив " + asset.getTicker() + " продан! Зачислено: " + netAmount + " " + asset.getCurrency() + " на счет " + selectedAccountNumber + ". Комиссия: " + commissionAmount + " " + asset.getCurrency() + ".");
                dialog.close();
            } catch (JsonProcessingException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить баланс или портфель: " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось выполнить транзакцию: " + e.getMessage());
            }
        });
    }

    private Map<String, Double> parseBalances(String balancesJson) throws JsonProcessingException {
        if (balancesJson == null || balancesJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        @SuppressWarnings("unchecked")
        Map<String, Double> balances = objectMapper.readValue(balancesJson, Map.class);
        return balances;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initialize();
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}