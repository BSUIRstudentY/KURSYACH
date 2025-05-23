package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.entity.Account;
import org.example.entity.User;
import org.example.repository.AccountRepository;
import org.example.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AccountsController {

    private static ConfigurableApplicationContext springContext;

    @FXML
    private VBox accountsContainer;

    @FXML
    private Label errorLabel;

    @FXML
    private Button addAccountButton;

    @FXML
    private Button deleteAccountButton;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    private User currentUser;

    @FXML
    public void initialize() {
        System.out.println("Initializing AccountsController");
        System.out.println("accountService: " + (accountService != null ? "not null" : "null"));
        System.out.println("objectMapper: " + (objectMapper != null ? "not null" : "null"));
        System.out.println("deleteAccountButton: " + (deleteAccountButton != null ? "not null" : "null"));

        if (accountsContainer == null) {
            System.err.println("accountsContainer is null. Check fx:id in accounts.fxml.");
            throw new IllegalStateException("accountsContainer is null. Check fx:id in accounts.fxml.");
        }

        if (deleteAccountButton == null) {
            System.err.println("deleteAccountButton is null. Check fx:id in accounts.fxml.");
            throw new IllegalStateException("deleteAccountButton is null. Check fx:id in accounts.fxml.");
        }

        if (currentUser != null) {
            updateAccountsTable();
        }

        deleteAccountButton.setOnAction(event -> {
            System.out.println("Delete account button action triggered.");
            deleteAccount();
        });
    }

    @FXML
    public void addAccount() {
        Dialog<Account> dialog = new Dialog<>();
        dialog.setTitle("Добавить новый счет");
        dialog.setHeaderText("Введите данные счета");

        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        ChoiceBox<Account.AccountType> typeChoiceBox = new ChoiceBox<>();
        typeChoiceBox.getItems().addAll(Account.AccountType.values());
        typeChoiceBox.setValue(Account.AccountType.ВАЛЮТНЫЙ);

        VBox content = new VBox(10);
        content.getChildren().add(new Label("Название счета:"));
        content.getChildren().add(nameField);
        content.getChildren().add(new Label("Тип счета:"));
        content.getChildren().add(typeChoiceBox);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Map<String, Double> balances = new HashMap<>();
                balances.put("USD", 0.0);
                balances.put("RUB", 0.0);
                balances.put("EUR", 0.0);
                balances.put("BYN", 0.0);

                Account account;
                try {
                    account = Account.builder()
                            .accountNumber("ACC-" + System.currentTimeMillis())
                            .name(nameField.getText().isEmpty() ? "БАЗОВЫЙ" : nameField.getText())
                            .accountType(typeChoiceBox.getValue())
                            .balances(objectMapper.writeValueAsString(balances))
                            .createdAt(LocalDateTime.now())
                            .user(currentUser)
                            .build();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                return account;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(account -> {
            try {
                accountService.createAccount(account);
                updateAccountsTable();
                errorLabel.setText("Счет успешно добавлен!");
            } catch (Exception e) {
                errorLabel.setText("Ошибка при добавлении счета: " + e.getMessage());
                System.err.println("Error adding account: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void deleteAccount() {
        System.out.println("Delete account button clicked.");
        List<Account> accounts = accountService.getAccountsByUser(currentUser);
        if (accounts == null || accounts.isEmpty()) {
            System.out.println("No accounts found for deletion.");
            errorLabel.setText("Счета для удаления не найдены.");
            return;
        }

        System.out.println("Creating context menu with " + accounts.size() + " accounts.");
        ContextMenu contextMenu = new ContextMenu();
        for (Account account : accounts) {
            MenuItem menuItem = new MenuItem(account.getName() + " (ID: " + account.getAccountId() + ")");
            menuItem.setOnAction(e -> {
                System.out.println("Selected account for deletion: " + account.getAccountId());
                handleDeleteAccount(account);
            });
            contextMenu.getItems().add(menuItem);
        }

        contextMenu.show(deleteAccountButton, deleteAccountButton.getScene().getWindow().getX() + deleteAccountButton.getLayoutX(),
                deleteAccountButton.getScene().getWindow().getY() + deleteAccountButton.getLayoutY() + deleteAccountButton.getHeight());
        System.out.println("Context menu shown.");
    }

    private void handleDeleteAccount(Account account) {
        System.out.println("Handling delete for account ID: " + account.getAccountId());
        Map<String, Double> balances;
        try {
            balances = parseBalances(account.getBalances());
        } catch (JsonProcessingException e) {
            balances = new HashMap<>();
            errorLabel.setText("Ошибка парсинга балансов: " + e.getMessage());
            System.err.println("Error parsing balances: " + e.getMessage());
            return;
        }

        boolean hasMoney = balances.values().stream().anyMatch(balance -> balance != null && balance > 0.0);

        if (hasMoney) {
            System.out.println("Account has money, showing confirmation dialog.");
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("На счете есть деньги!");
            alert.setContentText("На вашем счете есть деньги. При удалении счета все средства будут утеряны. Продолжить?");
            alert.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10;");

            ButtonType confirmButton = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(confirmButton, cancelButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == confirmButton) {
                    System.out.println("User confirmed deletion for account ID: " + account.getAccountId());
                    performAccountDeletion(account);
                } else {
                    System.out.println("User canceled deletion for account ID: " + account.getAccountId());
                }
            });
        } else {
            System.out.println("Account has no money, proceeding with deletion for account ID: " + account.getAccountId());
            performAccountDeletion(account);
        }
    }

    private void performAccountDeletion(Account account) {
        System.out.println("Performing deletion for account ID: " + account.getAccountId());
        try {
            accountService.deleteAccount(account);
            updateAccountsTable();
            errorLabel.setText("Счет успешно удален из списка!");
        } catch (Exception e) {
            errorLabel.setText("Ошибка при удалении счета из списка: " + e.getMessage());
            System.err.println("Error deleting account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateAccountsTable() {
        if (currentUser == null) {
            errorLabel.setText("Нет данных пользователя.");
            System.out.println("No user data available.");
            return;
        }
        try {
            List<Account> accounts = accountService.getAccountsByUser(currentUser);
            System.out.println("Updating table with accounts: " + (accounts != null ? accounts.size() : "null"));
            if (accounts == null || accounts.isEmpty()) {
                errorLabel.setText("Счета для этого пользователя не найдены.");
                accountsContainer.getChildren().clear();
            } else {
                accountsContainer.getChildren().clear();
                for (Account account : accounts) {
                    HBox accountBox = createAccountBox(account);
                    accountsContainer.getChildren().add(accountBox);
                }
                errorLabel.setText("");
            }
        } catch (Exception e) {
            errorLabel.setText("Ошибка загрузки счетов: " + e.getMessage());
            System.err.println("Error loading accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createAccountBox(Account account) {
        System.out.println("Creating account box for account ID: " + account.getAccountId());
        HBox box = new HBox(20);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), box);
        scaleIn.setToX(1.02);
        scaleIn.setToY(1.02);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), box);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        box.setOnMouseEntered(e -> {
            scaleIn.playFromStart();
            box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 4);");
        });
        box.setOnMouseExited(e -> {
            scaleOut.playFromStart();
            box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        });

        VBox nameBox = new VBox(5);
        Label nameLabel = new Label(account.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label accountNumberLabel = new Label(account.getAccountNumber());
        accountNumberLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        nameBox.getChildren().addAll(nameLabel, accountNumberLabel);

        Map<String, Double> balances;
        try {
            balances = parseBalances(account.getBalances());
        } catch (JsonProcessingException e) {
            balances = new HashMap<>();
            errorLabel.setText("Ошибка парсинга балансов: " + e.getMessage());
        }

        VBox balancesBox = new VBox(5);
        addBalanceLabel(balancesBox, "USD", balances.getOrDefault("USD", 0.0));
        addBalanceLabel(balancesBox, "RUB", balances.getOrDefault("RUB", 0.0));
        addBalanceLabel(balancesBox, "EUR", balances.getOrDefault("EUR", 0.0));
        addBalanceLabel(balancesBox, "BYN", balances.getOrDefault("BYN", 0.0));

        Label typeLabel = new Label(account.getAccountType().name());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-background-color: #6a11cb; " +
                "-fx-padding: 5 10; -fx-background-radius: 15;");

        HBox contentBox = new HBox(20);
        contentBox.setAlignment(javafx.geometry.Pos.CENTER);
        contentBox.getChildren().addAll(nameBox, balancesBox);
        box.getChildren().addAll(contentBox, typeLabel);
        HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);

        return box;
    }

    private void addBalanceLabel(VBox parent, String currency, Double amount) {
        HBox balanceRow = new HBox(8);
        balanceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        InputStream cardImageStream = getClass().getResourceAsStream("/card_icon.png");
        if (cardImageStream != null) {
            Image cardImage = new Image(cardImageStream);
            ImageView cardIcon = new ImageView(cardImage);
            cardIcon.setFitHeight(20);
            cardIcon.setFitWidth(20);
            balanceRow.getChildren().add(cardIcon);
        }

        Label label = new Label(String.format("%.2f %s", amount, currency));
        label.setStyle("-fx-font-size: 18px; -fx-text-fill: #333; -fx-font-weight: bold;");
        balanceRow.getChildren().add(label);

        parent.getChildren().add(balanceRow);
    }

    private Map<String, Double> parseBalances(String balancesJson) throws JsonProcessingException {
        if (balancesJson == null || balancesJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        @SuppressWarnings("unchecked")
        Map<String, Double> balances = objectMapper.readValue(balancesJson, Map.class);
        return balances;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateAccountsTable();
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}