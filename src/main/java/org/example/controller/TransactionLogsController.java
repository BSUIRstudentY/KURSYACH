package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entity.*;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionLogsController {

    @FXML
    private VBox transactionsContainer;

    @FXML
    private ChoiceBox<String> sortCriteriaBox;

    @FXML
    private Button sortDirectionButton;

    @FXML
    private ChoiceBox<String> typeFilterBox;

    @FXML
    private ChoiceBox<String> userFilterBox;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentAdmin;
    private boolean sortAscending = true;
    private String currentSortCriteria = "Дата";

    @FXML
    public void initialize() {
        transactionsContainer.setStyle("-fx-background-color: transparent;");

        // Инициализация критериев сортировки
        sortCriteriaBox.getItems().addAll("Дата", "Тип", "Сумма");
        sortCriteriaBox.setValue("Дата");
        sortCriteriaBox.setOnAction(e -> {
            currentSortCriteria = sortCriteriaBox.getValue();
            updateTransactionLogs();
        });

        // Инициализация фильтра по типу
        typeFilterBox.getItems().addAll("Все", "ПОКУПКА", "ПРОДАЖА", "ПОПОЛНЕНИЕ", "СНЯТИЕ", "ОБМЕН");
        typeFilterBox.setValue("Все");
        typeFilterBox.setOnAction(e -> updateTransactionLogs());

        // Инициализация фильтра по пользователям
        userFilterBox.getItems().add("Все пользователи");
        List<User> users = userRepository.findAll();
        users.forEach(user -> userFilterBox.getItems().add(user.getFirstName() + " " + user.getLastName() + " (" + user.getUserId() + ")"));
        userFilterBox.setValue("Все пользователи");
        userFilterBox.setOnAction(e -> updateTransactionLogs());

        updateTransactionLogs();
    }

    @FXML
    public void toggleSortDirection() {
        sortAscending = !sortAscending;
        sortDirectionButton.setText(sortAscending ? "↑" : "↓");
        updateTransactionLogs();
    }

    public void setCurrentAdmin(User admin) {
        this.currentAdmin = admin;
        updateTransactionLogs();
    }

    private void updateTransactionLogs() {
        transactionsContainer.getChildren().clear();

        if (currentAdmin == null) {
            transactionsContainer.getChildren().add(new Label("Пожалуйста, войдите как администратор."));
            return;
        }

        List<Transaction> transactions = transactionRepository.findAll();
        if (transactions.isEmpty()) {
            transactionsContainer.getChildren().add(new Label("История транзакций пуста."));
            return;
        }

        // Логирование для проверки
        System.out.println("Всего транзакций перед фильтрацией: " + transactions.size());

        // Фильтрация по типу транзакции
        String selectedType = typeFilterBox.getValue();
        if (!"Все".equals(selectedType)) {
            transactions = transactions.stream()
                    .filter(t -> t.getTransactionType() != null && t.getTransactionType().toString().equals(selectedType))
                    .collect(Collectors.toList());
            System.out.println("Транзакции после фильтрации по типу '" + selectedType + "': " + transactions.size());
        }

        // Фильтрация по пользователю
        String selectedUser = userFilterBox.getValue();
        if (!"Все пользователи".equals(selectedUser)) {
            try {

                String userIdStr = selectedUser.substring(selectedUser.lastIndexOf("(") + 1, selectedUser.lastIndexOf(")"));
                Integer userId = Integer.parseInt(userIdStr);

                transactions = transactionRepository.findByUser(userRepository.findById(userId).get());
                System.out.println("Транзакции после фильтрации по пользователю '" + selectedUser + "': " + transactions.size());
            } catch (Exception e) {
                System.out.println("Ошибка при фильтрации по пользователю '" + selectedUser + "': " + e.getMessage());
                transactionsContainer.getChildren().add(new Label("Ошибка при фильтрации по пользователю."));
                return;
            }
        }

        // Сортировка
        Comparator<Transaction> comparator = switch (currentSortCriteria) {
            case "Дата" -> Comparator.comparing(Transaction::getTransactionDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "Тип" -> Comparator.comparing(t -> t.getTransactionType() != null ? t.getTransactionType().toString() : "", Comparator.nullsLast(Comparator.naturalOrder()));
            case "Сумма" -> Comparator.comparing(Transaction::getAmount, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Transaction::getTransactionDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        transactions.sort(comparator);

        // Отображение транзакций
        if (transactions.isEmpty()) {
            transactionsContainer.getChildren().add(new Label("Транзакций для выбранного пользователя не найдено."));
            return;
        }

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
        Label dateLabel = new Label(transaction.getTransactionDate() != null ? transaction.getTransactionDate().toString() : "Дата неизвестна");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        infoBox.getChildren().addAll(typeLabel, detailLabel, dateLabel);
        box.getChildren().add(infoBox);

        return box;
    }

    private String getTransactionDescription(Transaction transaction) {
        User user = transaction.getUser();
        String userInfo = user != null ? user.getFirstName() + " " + user.getLastName() + " (" + user.getUserId() + ")" : "Неизвестный пользователь";
        switch (transaction.getTransactionType()) {
            case ПОКУПКА:
                Asset asset = transaction.getAsset();
                return String.format("%s: Куплен %s (%s) в количестве %.2f", userInfo,
                        asset != null ? asset.getTicker() : "Неизвестный актив",
                        asset != null ? asset.getName() : "N/A",
                        transaction.getQuantity() != null ? transaction.getQuantity() : 0.0);
            case ПРОДАЖА:
                asset = transaction.getAsset();
                return String.format("%s: Продан %s (%s) в количестве %.2f", userInfo,
                        asset != null ? asset.getTicker() : "Неизвестный актив",
                        asset != null ? asset.getName() : "N/A",
                        transaction.getQuantity() != null ? transaction.getQuantity() : 0.0);
            case ПОПОЛНЕНИЕ:
                Account toAccount = transaction.getToAccount();
                return String.format("%s: Пополнение на счет %s", userInfo, toAccount != null ? toAccount.getAccountNumber() : "Не указан");
            case СНЯТИЕ:
                Account fromAccount = transaction.getFromAccount();
                return String.format("%s: Снятие с счета %s", userInfo, fromAccount != null ? fromAccount.getAccountNumber() : "Не указан");
            case ОБМЕН:
                String toCurrency = transaction.getToCurrency() != null ? transaction.getToCurrency() : "Не указана";
                return String.format("%s: Обмен: %s → %s", userInfo, transaction.getCurrency(), toCurrency);
            default:
                return userInfo + ": Неизвестная операция";
        }
    }

    private String getTransactionDetail(Transaction transaction) {
        switch (transaction.getTransactionType()) {
            case ПОКУПКА:
            case ПРОДАЖА:
                Asset asset = transaction.getAsset();
                if (asset == null) {
                    return "Актив не найден.";
                }
                BigDecimal totalPrice = (asset.getCurrentPrice() != null && transaction.getQuantity() != null)
                        ? asset.getCurrentPrice().multiply(BigDecimal.valueOf(transaction.getQuantity()))
                        : BigDecimal.ZERO;
                return String.format("Цена: %.2f %s за единицу, Итого: %.2f %s",
                        asset.getCurrentPrice() != null ? asset.getCurrentPrice() : BigDecimal.ZERO,
                        asset.getCurrency() != null ? asset.getCurrency() : "N/A",
                        totalPrice,
                        asset.getCurrency() != null ? asset.getCurrency() : "N/A");
            case ПОПОЛНЕНИЕ:
            case СНЯТИЕ:
                return String.format("Сумма: %.2f %s",
                        transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO,
                        transaction.getCurrency() != null ? transaction.getCurrency() : "N/A");
            case ОБМЕН:
                String toCurrency = transaction.getToCurrency();
                if (toCurrency == null) {
                    return String.format("Сумма: %.2f %s, Получено: Неизвестно",
                            transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO,
                            transaction.getCurrency() != null ? transaction.getCurrency() : "N/A");
                }
                return String.format("Сумма: %.2f %s, Валюта назначения: %s",
                        transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO,
                        transaction.getCurrency() != null ? transaction.getCurrency() : "N/A",
                        toCurrency);
            default:
                return "";
        }
    }

    private String getColorForTransaction(Transaction transaction) {
        if (transaction.getTransactionType() == null) {
            return "#333"; // По умолчанию
        }
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
}