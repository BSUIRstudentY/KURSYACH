package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.entity.Account;
import org.example.entity.Commission;
import org.example.entity.CommissionType;
import org.example.entity.Transaction;
import org.example.entity.User;
import org.example.repository.AccountRepository;
import org.example.repository.CommissionRepository;
import org.example.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class TransactionsController {
    private static ConfigurableApplicationContext springContext;

    @FXML
    private Label modeLabel;

    @FXML
    private VBox contentBox;

    @FXML
    private Label titleLabel;

    @FXML
    private ChoiceBox<Account> accountBox;

    @FXML
    private ChoiceBox<String> currencyBox;

    @FXML
    private ChoiceBox<String> methodBox;

    @FXML
    private TextField amountField;

    @FXML
    private Button submitButton;

    @FXML
    private Label errorLabel;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private User currentUser;
    private boolean isDepositMode = true;

    @FXML
    public void initialize() {
        modeLabel.setText("Пополнение / Снятие");
        setupModeButtons();

        currencyBox.setItems(FXCollections.observableArrayList("USD", "RUB", "EUR", "BYN"));
        currencyBox.setValue("USD");

        accountBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Account account) {
                return account != null ? account.getName() + " (" + account.getAccountNumber() + ")" : "";
            }
            @Override
            public Account fromString(String string) { return null; }
        });

        methodBox.setItems(FXCollections.observableArrayList(
                "Банковская карта", "Криптовалюта", "Банковский перевод"
        ));
        methodBox.setValue("Банковская карта");

        if (currentUser != null && !accountRepository.findByUser(currentUser).isEmpty()) {
            accountBox.setItems(FXCollections.observableArrayList(accountRepository.findByUser(currentUser)));
            accountBox.setValue(accountRepository.findByUser(currentUser).get(0));
        }

        addHoverAnimation(accountBox);
        addHoverAnimation(currencyBox);
        addHoverAnimation(methodBox);
        addHoverAnimation(amountField);
        addHoverAnimation(submitButton);
    }

    private void addHoverAnimation(Control control) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), control);
        scaleIn.setToX(1.02);
        scaleIn.setToY(1.02);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), control);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        control.setOnMouseEntered(e -> scaleIn.playFromStart());
        control.setOnMouseExited(e -> scaleOut.playFromStart());
    }

    private void setupModeButtons() {
        Hyperlink depositLink = new Hyperlink("Пополнение");
        Hyperlink withdrawalLink = new Hyperlink("Снятие");

        depositLink.setStyle("-fx-font-size: 14px; -fx-text-fill: #6a11cb;");
        withdrawalLink.setStyle("-fx-font-size: 14px; -fx-text-fill: #6a11cb;");

        depositLink.setOnAction(e -> switchMode(true));
        withdrawalLink.setOnAction(e -> switchMode(false));

        modeLabel.setGraphic(new HBox(10, depositLink, withdrawalLink));
        switchMode(true);
    }

    private void switchMode(boolean isDeposit) {
        this.isDepositMode = isDeposit;
        titleLabel.setText(isDeposit ? "Пополнение счета" : "Снятие средств");
        ((Hyperlink) ((HBox) modeLabel.getGraphic()).getChildren().get(isDeposit ? 0 : 1)).setStyle("-fx-font-size: 14px; -fx-text-fill: #6a11cb; -fx-underline: true;");
        ((Hyperlink) ((HBox) modeLabel.getGraphic()).getChildren().get(isDeposit ? 1 : 0)).setStyle("-fx-font-size: 14px; -fx-text-fill: #6a11cb; -fx-underline: false;");
        contentBox.setDisable(false);
        animateTransition();
    }

    private void animateTransition() {
        FadeTransition ft = new FadeTransition(Duration.millis(500), contentBox);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        contentBox.setOpacity(0);
        ft.play();
    }

    @FXML
    public void submitTransaction() {
        try {
            errorLabel.setText("");

            if (currentUser == null || accountRepository.findByUser(currentUser).isEmpty()) {
                errorLabel.setText("У пользователя нет счетов.");
                return;
            }

            Account account = accountBox.getValue();
            if (account == null) {
                errorLabel.setText("Выберите счет.");
                return;
            }

            String currency = currencyBox.getValue();
            if (currency == null) {
                errorLabel.setText("Выберите валюту.");
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountField.getText());
            } catch (NumberFormatException e) {
                errorLabel.setText("Введите корректную сумму.");
                return;
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                errorLabel.setText("Сумма должна быть положительной.");
                return;
            }

            Map<String, Double> balances = parseBalances(account.getBalances());

            Commission commission = commissionRepository.findByCommissionType(
                            isDepositMode ? CommissionType.ПОПОЛНЕНИЕ : CommissionType.ПОКУПКА)
                    .orElse(Commission.builder()
                            .amount(BigDecimal.ZERO)
                            .currency("USD")
                            .commissionDate(LocalDateTime.now())
                            .commissionType(isDepositMode ? CommissionType.ПОПОЛНЕНИЕ : CommissionType.ПОКУПКА)
                            .percentage(BigDecimal.ZERO)
                            .isActive(true)
                            .build());

            BigDecimal commissionAmount = commission.getAmount().add(amount.multiply(commission.getPercentage().divide(new BigDecimal("100"))));
            BigDecimal totalAmount = isDepositMode ? amount : amount.add(commissionAmount);

            if (!isDepositMode) {
                double currentBalance = balances.getOrDefault(currency, 0.0);
                if (BigDecimal.valueOf(currentBalance).compareTo(totalAmount) < 0) {
                    errorLabel.setText("Недостаточно средств с учетом комиссии (" + commissionAmount.setScale(2) + " " + commission.getCurrency() + ").");
                    return;
                }
            }

            Transaction transaction = Transaction.builder()
                    .user(currentUser)
                    .transactionType(isDepositMode ? Transaction.TransactionType.ПОПОЛНЕНИЕ : Transaction.TransactionType.СНЯТИЕ)
                    .amount(totalAmount)
                    .currency(currency)
                    .transactionDate(LocalDateTime.now())
                    .toAccount(isDepositMode ? account : null)
                    .fromAccount(isDepositMode ? null : account)
                    .build();

            transactionService.createTransaction(transaction, account, balances);

            errorLabel.setText("Операция успешно выполнена! Комиссия: " + commissionAmount.setScale(2) + " " + commission.getCurrency());
            amountField.clear();
            animateSuccess();
        } catch (Exception e) {
            errorLabel.setText("Ошибка: " + e.getMessage());
            System.err.println("Error during transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void animateSuccess() {
        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setAutoReverse(true);
        ft.setCycleCount(2);
        ft.play();
    }

    private Map<String, Double> parseBalances(String balancesJson) {
        try {
            if (balancesJson == null || balancesJson.trim().isEmpty()) {
                return new HashMap<>();
            }
            @SuppressWarnings("unchecked")
            Map<String, Double> balances = objectMapper.readValue(balancesJson, Map.class);
            return balances;
        } catch (JsonProcessingException e) {
            errorLabel.setText("Ошибка парсинга баланса.");
            System.err.println("Error parsing balances: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null && !accountRepository.findByUser(currentUser).isEmpty()) {
            accountBox.setItems(FXCollections.observableArrayList(accountRepository.findByUser(currentUser)));
            accountBox.setValue(accountRepository.findByUser(currentUser).get(0));
        }
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}