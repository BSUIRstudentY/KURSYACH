package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.example.entity.Account;
import org.example.entity.Commission;
import org.example.entity.CommissionType;
import org.example.entity.ExchangeRate;
import org.example.entity.User;
import org.example.repository.AccountRepository;
import org.example.repository.CommissionRepository;
import org.example.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class ExchangeController {
    private static ConfigurableApplicationContext springContext;

    @FXML
    private ChoiceBox<Account> fromAccountBox;

    @FXML
    private ChoiceBox<Account> toAccountBox;

    @FXML
    private ChoiceBox<String> fromCurrencyBox;

    @FXML
    private ChoiceBox<String> toCurrencyBox;

    @FXML
    private TextField amountField;

    @FXML
    private TextField convertedAmountField;

    @FXML
    private Button exchangeButton;

    @FXML
    private Label errorLabel;

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommissionRepository commissionRepository;

    private User currentUser;
    @Autowired
    private AccountRepository accountRepository;

    @FXML
    public void initialize() {
        fromCurrencyBox.setItems(FXCollections.observableArrayList("USD", "RUB", "EURO", "BYN"));
        toCurrencyBox.setItems(FXCollections.observableArrayList("USD", "RUB", "EURO", "BYN"));
        fromCurrencyBox.setValue("RUB");
        toCurrencyBox.setValue("USD");

        fromAccountBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Account account) {
                return account != null ? account.getName() + " (" + account.getAccountNumber() + ")" : "";
            }
            @Override
            public Account fromString(String string) { return null; }
        });

        toAccountBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Account account) {
                return account != null ? account.getName() + " (" + account.getAccountNumber() + ")" : "";
            }
            @Override
            public Account fromString(String string) { return null; }
        });

        if (currentUser != null && !accountRepository.findByUser(currentUser).isEmpty()) {
            fromAccountBox.setItems(FXCollections.observableArrayList(accountRepository.findByUser(currentUser)));
            toAccountBox.setItems(FXCollections.observableArrayList(accountRepository.findByUser(currentUser)));
            fromAccountBox.setValue(accountRepository.findByUser(currentUser).get(0));
            toAccountBox.setValue(accountRepository.findByUser(currentUser).get(0));
        }

        fromCurrencyBox.valueProperty().addListener((obs, oldVal, newVal) -> updateConvertedAmount());
        toCurrencyBox.valueProperty().addListener((obs, oldVal, newVal) -> updateConvertedAmount());
        amountField.textProperty().addListener((obs, oldVal, newVal) -> updateConvertedAmount());

        addHoverAnimation(fromAccountBox);
        addHoverAnimation(toAccountBox);
        addHoverAnimation(fromCurrencyBox);
        addHoverAnimation(toCurrencyBox);
        addHoverAnimation(amountField);
        addHoverAnimation(convertedAmountField);
        addHoverAnimation(exchangeButton);
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

    @FXML
    public void calculateConvertedAmount(javafx.event.Event event) {
        updateConvertedAmount();
    }

    private void updateConvertedAmount() {
        try {
            String fromCurrency = fromCurrencyBox.getValue();
            String toCurrency = toCurrencyBox.getValue();
            BigDecimal amount = new BigDecimal(amountField.getText().isEmpty() ? "0" : amountField.getText());

            if (fromCurrency == null || toCurrency == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                convertedAmountField.setText("");
                return;
            }

            if (fromCurrency.equals(toCurrency)) {
                convertedAmountField.setText(amount.setScale(2, BigDecimal.ROUND_HALF_UP) + " " + toCurrency);
            } else {
                ExchangeRate exchangeRate = exchangeService.getExchangeRate(fromCurrency, toCurrency);
                BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate());
                convertedAmountField.setText(convertedAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + " " + toCurrency);
            }
        } catch (NumberFormatException e) {
            convertedAmountField.setText("Неверный формат суммы");
        } catch (Exception e) {
            convertedAmountField.setText("Ошибка расчета");
            errorLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    public void exchangeCurrency() {
        try {
            if (currentUser == null || accountRepository.findByUser(currentUser).isEmpty()) {
                errorLabel.setText("У пользователя нет счетов.");
                return;
            }

            Account fromAccount = fromAccountBox.getValue();
            Account toAccount = toAccountBox.getValue();
            String fromCurrency = fromCurrencyBox.getValue();
            String toCurrency = toCurrencyBox.getValue();
            BigDecimal amount;

            try {
                amount = new BigDecimal(amountField.getText());
            } catch (NumberFormatException e) {
                errorLabel.setText("Введите корректную сумму.");
                return;
            }

            if (fromAccount == null || toAccount == null) {
                errorLabel.setText("Выберите счета для обмена.");
                return;
            }

            if (fromAccount.equals(toAccount) && fromCurrency.equals(toCurrency)) {
                errorLabel.setText("Счета и валюты должны быть разными.");
                return;
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                errorLabel.setText("Сумма должна быть положительной.");
                return;
            }

            Map<String, Double> fromBalances = parseBalances(fromAccount.getBalances());
            double fromBalance = fromBalances.getOrDefault(fromCurrency, 0.0);
            if (BigDecimal.valueOf(fromBalance).compareTo(amount) < 0) {
                errorLabel.setText("Недостаточно средств на счете в валюте " + fromCurrency + ".");
                return;
            }

            // Получаем комиссию для типа EXCHANGE
            Commission commission = commissionRepository.findByCommissionType(CommissionType.ОБМЕН)
                    .orElse(Commission.builder()
                            .amount(BigDecimal.ZERO)
                            .currency("USD")
                            .commissionDate(LocalDateTime.now())
                            .commissionType(CommissionType.ОБМЕН)
                            .percentage(BigDecimal.ZERO)
                            .isActive(true)
                            .build());

            BigDecimal commissionAmount = commission.getAmount().add(amount.multiply(commission.getPercentage().divide(new BigDecimal("100"))));
            BigDecimal totalAmountToDeduct = amount.add(commissionAmount);

            if (BigDecimal.valueOf(fromBalance).compareTo(totalAmountToDeduct) < 0) {
                errorLabel.setText("Недостаточно средств с учетом комиссии (" + commissionAmount.setScale(2) + " " + commission.getCurrency() + ").");
                return;
            }

            BigDecimal convertedAmount = amount;
            if (!fromCurrency.equals(toCurrency)) {
                ExchangeRate exchangeRate = exchangeService.getExchangeRate(fromCurrency, toCurrency);
                convertedAmount = amount.multiply(exchangeRate.getRate());
            }

            exchangeService.exchangeCurrency(fromAccount, toAccount, fromCurrency, toCurrency, totalAmountToDeduct, convertedAmount);
            errorLabel.setText("Обмен успешно выполнен! Комиссия: " + commissionAmount.setScale(2) + " " + commission.getCurrency());
            amountField.clear();
            updateConvertedAmount();
        } catch (Exception e) {
            errorLabel.setText("Ошибка обмена: " + e.getMessage());
            System.err.println("Error during exchange: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, Double> parseBalances(String balancesJson) {
        try {
            if (balancesJson == null || balancesJson.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(balancesJson, Map.class);
        } catch (JsonProcessingException e) {
            System.err.println("Error parsing balances: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null && !accountRepository.findByUser(currentUser).isEmpty()) {
            fromAccountBox.setItems(FXCollections.observableArrayList(accountRepository.findByUser(currentUser)));
            toAccountBox.setItems(FXCollections.observableArrayList(accountRepository.findByUser(currentUser)));
            fromAccountBox.setValue(accountRepository.findByUser(currentUser).get(0));
            toAccountBox.setValue(accountRepository.findByUser(currentUser).get(0));
            updateConvertedAmount();
        }
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}