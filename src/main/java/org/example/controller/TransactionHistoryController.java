package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entity.*;
import org.example.service.AssetService;
import org.example.service.ExchangeService;
import org.example.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionHistoryController {
    private static ConfigurableApplicationContext springContext;

    @FXML
    private VBox transactionsContainer;

    @FXML
    private ChoiceBox<String> sortCriteriaBox;

    @FXML
    private Button sortDirectionButton;

    @FXML
    private ChoiceBox<String> typeFilterBox;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private ExchangeService exchangeService;

    private User currentUser;
    private boolean sortAscending = true; // По умолчанию сортировка по возрастанию
    private String currentSortCriteria = "Дата"; // По умолчанию сортировка по дате

    @FXML
    public void initialize() {
        transactionsContainer.setStyle("-fx-background-color: transparent;");

        // Инициализация критериев сортировки
        sortCriteriaBox.getItems().addAll("Дата", "Тип", "Сумма");
        sortCriteriaBox.setValue("Дата");
        sortCriteriaBox.setOnAction(e -> {
            currentSortCriteria = sortCriteriaBox.getValue();
            updateTransactionHistory();
        });

        // Инициализация фильтра по типу
        typeFilterBox.getItems().addAll("Все", "ПОКУПКА", "ПРОДАЖА", "ПОПОЛНЕНИЕ", "СНЯТИЕ", "ОБМЕН");
        typeFilterBox.setValue("Все");
        typeFilterBox.setOnAction(e -> updateTransactionHistory());

        updateTransactionHistory();
    }

    @FXML
    public void toggleSortDirection() {
        sortAscending = !sortAscending;
        sortDirectionButton.setText(sortAscending ? "↑" : "↓");
        updateTransactionHistory();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Setting currentUser in TransactionHistoryController: " + (user != null ? "userId = " + user.getUserId() + ", email = " + user.getEmail() : "null"));
        updateTransactionHistory();
    }

    private void updateTransactionHistory() {
        transactionsContainer.getChildren().clear();
        System.out.println("Current User: " + (currentUser != null ? "userId = " + currentUser.getUserId() + ", email = " + currentUser.getEmail() : "null"));

        if (currentUser == null) {
            transactionsContainer.getChildren().add(new Label("Пожалуйста, войдите в систему."));
            System.out.println("No current user set.");
            return;
        }

        List<Transaction> transactions = transactionService.findByUser(currentUser);
        System.out.println("Found " + transactions.size() + " transactions for user: " + currentUser.getEmail());

        if (transactions.isEmpty()) {
            transactionsContainer.getChildren().add(new Label("История операций пуста."));
            System.out.println("No transactions found.");
            return;
        }

        // Фильтрация по типу транзакции
        String selectedType = typeFilterBox.getValue();
        if (!"Все".equals(selectedType)) {
            transactions = transactions.stream()
                    .filter(t -> t.getTransactionType().toString().equals(selectedType))
                    .collect(Collectors.toList());
        }

        // Сортировка
        Comparator<Transaction> comparator = switch (currentSortCriteria) {
            case "Дата" -> Comparator.comparing(Transaction::getTransactionDate);
            case "Тип" -> Comparator.comparing(t -> t.getTransactionType().toString());
            case "Сумма" -> Comparator.comparing(Transaction::getAmount);
            default -> Comparator.comparing(Transaction::getTransactionDate); // По умолчанию по дате
        };

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        transactions.sort(comparator);

        // Отображение транзакций
        for (Transaction transaction : transactions) {
            HBox transactionBox = createTransactionBox(transaction);
            transactionsContainer.getChildren().add(transactionBox);
        }
    }

    private HBox createTransactionBox(Transaction transaction) {
        HBox box = new HBox(20);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 4);"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"));

        VBox infoBox = new VBox(5);
        Label typeLabel = new Label(getTransactionDescription(transaction));
        typeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + getColorForTransaction(transaction) + ";");
        Label detailLabel = new Label(getTransactionDetail(transaction));
        detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label dateLabel = new Label(transaction.getTransactionDate().toString());
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        infoBox.getChildren().addAll(typeLabel, detailLabel, dateLabel);
        box.getChildren().add(infoBox);

        return box;
    }

    private String getTransactionDescription(Transaction transaction) {
        switch (transaction.getTransactionType()) {
            case ПОКУПКА:
                Asset asset = transaction.getAsset();
                return String.format("Куплен %s (%s) в количестве %.2f",
                        asset.getTicker(), asset.getName(), transaction.getQuantity());
            case ПРОДАЖА:
                asset = transaction.getAsset();
                return String.format("Продан %s (%s) в количестве %.2f",
                        asset.getTicker(), asset.getName(), transaction.getQuantity());
            case ПОПОЛНЕНИЕ:
                Account toAccount = transaction.getToAccount();
                return String.format("Пополнение на счет %s",
                        toAccount != null ? toAccount.getAccountNumber() : "Не указан");
            case СНЯТИЕ:
                Account fromAccount = transaction.getFromAccount();
                return String.format("Снятие с счета %s",
                        fromAccount != null ? fromAccount.getAccountNumber() : "Не указан");
            case ОБМЕН:
                String toCurrency = transaction.getToCurrency() != null ? transaction.getToCurrency() : "Не указана";
                return String.format("Обмен: %s → %s",
                        transaction.getCurrency(), toCurrency);
            default:
                return "Неизвестная операция";
        }
    }

    private String getTransactionDetail(Transaction transaction) {
        switch (transaction.getTransactionType()) {
            case ПОКУПКА:
            case ПРОДАЖА:
                Asset asset = transaction.getAsset();
                BigDecimal totalPrice = asset.getCurrentPrice().multiply(BigDecimal.valueOf(transaction.getQuantity()));
                return String.format("Цена: %.2f %s за единицу, Итого: %.2f %s",
                        asset.getCurrentPrice(), asset.getCurrency(), totalPrice, asset.getCurrency());
            case ПОПОЛНЕНИЕ:
                return String.format("Сумма: %.2f %s",
                        transaction.getAmount(), transaction.getCurrency());
            case СНЯТИЕ:
                return String.format("Сумма: %.2f %s",
                        transaction.getAmount(), transaction.getCurrency());
            case ОБМЕН:
                if (transaction.getToCurrency() == null) {
                    return String.format("Сумма: %.2f %s, Получено: Неизвестно",
                            transaction.getAmount(), transaction.getCurrency());
                }
                try {
                    ExchangeRate rate = exchangeService.getExchangeRate(transaction.getCurrency(), transaction.getToCurrency());
                    BigDecimal convertedAmount = transaction.getAmount().multiply(rate.getRate());
                    return String.format("Сумма: %.2f %s, Получено: %.2f %s",
                            transaction.getAmount(), transaction.getCurrency(), convertedAmount, transaction.getToCurrency());
                } catch (Exception e) {
                    return "Курс обмена не найден.";
                }
            default:
                return "";
        }
    }

    private String getColorForTransaction(Transaction transaction) {
        switch (transaction.getTransactionType()) {
            case ПОКУПКА:
            case ПОПОЛНЕНИЕ:
                return "#4CAF50"; // Зеленый
            case ПРОДАЖА:
            case СНЯТИЕ:
                return "#F44336"; // Красный
            case ОБМЕН:
                return "#6a11cb"; // Фиолетовый
            default:
                return "#333"; // По умолчанию
        }
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}