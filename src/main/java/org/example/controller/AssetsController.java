package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.entity.Account;
import org.example.entity.Asset;
import org.example.entity.Commission;
import org.example.entity.CommissionType;
import org.example.entity.Transaction;
import org.example.entity.User;
import org.example.repository.CommissionRepository;
import org.example.repository.UserRepository;
import org.example.service.AssetService;
import org.example.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AssetsController {
    private static ConfigurableApplicationContext springContext;

    @FXML
    private Label modeLabel;

    @FXML
    private VBox assetsContainer;

    @FXML
    private ChoiceBox<String> sortCriteriaBox;

    @FXML
    private Button sortDirectionButton;

    @FXML
    private ChoiceBox<String> currencyFilterBox;

    @FXML
    private TextField searchField;

    @Autowired
    private AssetService assetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommissionRepository commissionRepository;

    private User currentUser;
    private String currentMode = "АКЦИИ"; // Default mode
    private boolean sortAscending = true; // По умолчанию сортировка по возрастанию
    private String currentSortCriteria = "Название"; // По умолчанию сортировка по названию

    @FXML
    public void initialize() {
        setupModeLinks();
        assetsContainer.setStyle("-fx-background-color: transparent;");

        sortCriteriaBox.getItems().addAll("Название", "Тикер", "Цена");
        sortCriteriaBox.setValue("Название");
        sortCriteriaBox.setOnAction(e -> {
            currentSortCriteria = sortCriteriaBox.getValue();
            updateAssetsTable();
        });

        currencyFilterBox.getItems().addAll("Все", "USD", "RUB", "EUR", "BYN");
        currencyFilterBox.setValue("Все");
        currencyFilterBox.setOnAction(e -> updateAssetsTable());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateAssetsTable());

        updateAssetsTable();
    }

    @FXML
    public void toggleSortDirection() {
        sortAscending = !sortAscending;
        sortDirectionButton.setText(sortAscending ? "↑" : "↓");
        updateAssetsTable();
    }

    private void setupModeLinks() {
        Hyperlink[] links = {
                new Hyperlink("АКЦИИ"), new Hyperlink("ОБЛИГАЦИИ"),
                new Hyperlink("ETF"), new Hyperlink("ТРЕЙДИНГ")
        };
        HBox linkBox = new HBox(10, links);
        linkBox.setStyle("-fx-alignment: center;");
        modeLabel.setGraphic(linkBox);

        for (Hyperlink link : links) {
            link.setStyle("-fx-text-fill: #555; -fx-cursor: hand;");
            link.setOnAction(ev -> {
                currentMode = link.getText();
                updateAssetsTable();
            });
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateAssetsTable();
    }

    private void updateAssetsTable() {
        assetsContainer.getChildren().clear();

        List<Asset> allAssets = assetService.findAll();

        List<Asset> filteredAssets = allAssets.stream()
                .filter(asset -> asset.getAssetType().name().equals(currentMode))
                .toList();

        String selectedCurrency = currencyFilterBox.getValue();
        if (!"Все".equals(selectedCurrency)) {
            filteredAssets = filteredAssets.stream()
                    .filter(asset -> asset.getCurrency().equals(selectedCurrency))
                    .toList();
        }

        String searchText = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        if (!searchText.isEmpty()) {
            filteredAssets = filteredAssets.stream()
                    .filter(asset -> asset.getName().toLowerCase().contains(searchText) ||
                            asset.getTicker().toLowerCase().contains(searchText))
                    .toList();
        }

        Comparator<Asset> comparator = switch (currentSortCriteria) {
            case "Название" -> Comparator.comparing(Asset::getName);
            case "Тикер" -> Comparator.comparing(Asset::getTicker);
            case "Цена" -> Comparator.comparing(Asset::getCurrentPrice);
            default -> Comparator.comparing(Asset::getName);
        };

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        filteredAssets = filteredAssets.stream().sorted(comparator).toList();

        HBox mainBox = new HBox(20);
        mainBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        VBox accountInfo = new VBox(5);
        Label accountNameLabel = new Label(currentUser != null && !currentUser.getAccounts().isEmpty() ? currentUser.getAccounts().get(0).getName() : "Нет счета");
        accountNameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label accountNumberLabel = new Label(currentUser != null && !currentUser.getAccounts().isEmpty() ? currentUser.getAccounts().get(0).getAccountNumber() : "Нет счета");
        accountNumberLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        accountInfo.getChildren().addAll(accountNameLabel, accountNumberLabel);

        VBox currencySummary = new VBox(5);
        BigDecimal totalUSD = BigDecimal.ZERO;
        BigDecimal totalRUB = BigDecimal.ZERO;
        BigDecimal totalEUR = BigDecimal.ZERO;
        BigDecimal totalBYN = BigDecimal.ZERO;

        for (Asset asset : filteredAssets) {
            BigDecimal price = asset.getCurrentPrice();
            BigDecimal value = price;
            switch (asset.getCurrency()) {
                case "USD" -> totalUSD = totalUSD.add(value);
                case "RUB" -> totalRUB = totalRUB.add(value);
                case "EUR" -> totalEUR = totalEUR.add(value);
                case "BYN" -> totalBYN = totalBYN.add(value);
            }
        }

        Label usdLabel = new Label(String.format("%.2f USD", totalUSD));
        usdLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");
        Label rubLabel = new Label(String.format("%.2f RUB", totalRUB));
        rubLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");
        Label eurLabel = new Label(String.format("%.2f EUR", totalEUR));
        eurLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");
        Label bynLabel = new Label(String.format("%.2f BYN", totalBYN));
        bynLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");

        currencySummary.getChildren().addAll(usdLabel, rubLabel, eurLabel, bynLabel);

        Button actionButton = new Button(currentMode);
        actionButton.setStyle("-fx-background-color: #6B46C1; -fx-text-fill: white; -fx-padding: 5 15; -fx-border-radius: 5; -fx-background-radius: 5;");
        actionButton.setOnAction(e -> System.out.println("Button clicked for " + currentMode));

        mainBox.getChildren().addAll(accountInfo, currencySummary, actionButton);
        HBox.setHgrow(accountInfo, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(currencySummary, javafx.scene.layout.Priority.ALWAYS);

        assetsContainer.getChildren().add(mainBox);

        for (Asset asset : filteredAssets) {
            HBox assetBox = createAssetBox(asset);
            assetsContainer.getChildren().add(assetBox);
        }
    }

    private HBox createAssetBox(Asset asset) {
        HBox box = new HBox(20);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 4);"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"));

        VBox infoBox = new VBox(5);
        Label tickerLabel = new Label(asset.getTicker());
        tickerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label nameLabel = new Label(asset.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        Label priceLabel = new Label(String.format("%.2f %s", asset.getCurrentPrice(), asset.getCurrency()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");

        // Получаем комиссию для текущего типа актива
        Commission assetCommission = commissionRepository.findByCommissionType(CommissionType.valueOf(currentMode))
                .orElse(Commission.builder()
                        .amount(BigDecimal.ZERO)
                        .currency(asset.getCurrency())
                        .commissionDate(LocalDateTime.now())
                        .commissionType(CommissionType.valueOf(currentMode))
                        .percentage(BigDecimal.ZERO)
                        .isActive(true)
                        .build());
        Label commissionLabel = new Label(String.format("Комиссия: %.2f %s (%.2f%%)", assetCommission.getAmount(), assetCommission.getCurrency(), assetCommission.getPercentage()));
        commissionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff4444;");

        infoBox.getChildren().addAll(tickerLabel, nameLabel, priceLabel, commissionLabel);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);

        Button buyButton = new Button("Купить");
        buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        buyButton.setOnMouseEntered(e -> buyButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 4);"));
        buyButton.setOnMouseExited(e -> buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        buyButton.setOnAction(e -> handleBuyAsset(asset));

        box.getChildren().addAll(infoBox, buyButton);
        return box;
    }

    private void handleBuyAsset(Asset asset) {
        if (currentUser == null) {
            showAlert("Ошибка", "Пожалуйста, войдите в систему, чтобы купить актив.");
            return;
        }

        List<Account> accounts = currentUser.getAccounts();
        if (accounts == null || accounts.isEmpty()) {
            showAlert("Ошибка", "У вас нет доступных счетов для покупки.");
            return;
        }

        List<String> accountNumbers = accounts.stream()
                .map(Account::getAccountNumber)
                .collect(Collectors.toList());

        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Покупка актива: " + asset.getTicker());
        dialog.setHeaderText("Выберите счет и укажите количество для покупки");
        dialog.getDialogPane().setStyle("-fx-background-color: #f4f4f4; -fx-padding: 20; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        ButtonType buyButtonType = new ButtonType("Купить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buyButtonType, ButtonType.CANCEL);

        VBox dialogContent = new VBox(15);
        dialogContent.setAlignment(javafx.geometry.Pos.CENTER);
        dialogContent.setStyle("-fx-padding: 15;");

        Label priceLabel = new Label(String.format("Цена: %.2f %s", asset.getCurrentPrice(), asset.getCurrency()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-font-weight: bold;");

        Label accountLabel = new Label("Счет:");
        accountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        ComboBox<String> accountComboBox = new ComboBox<>();
        accountComboBox.getItems().addAll(accountNumbers);
        accountComboBox.setPromptText("Выберите счет");
        accountComboBox.setStyle("-fx-pref-width: 250; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label quantityLabel = new Label("Количество:");
        quantityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Введите количество (например, 0.1)");
        quantityField.setStyle("-fx-pref-width: 250; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        Label balanceLabel = new Label("Проверьте баланс перед покупкой");
        balanceLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        dialogContent.getChildren().addAll(
                priceLabel,
                accountLabel,
                accountComboBox,
                quantityLabel,
                quantityField,
                balanceLabel
        );
        dialog.getDialogPane().setContent(dialogContent);

        Node buyButton = dialog.getDialogPane().lookupButton(buyButtonType);
        buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-radius: 5; -fx-background-radius: 5;");
        buyButton.setOnMouseEntered(e -> buyButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-radius: 5; -fx-background-radius: 5;"));
        buyButton.setOnMouseExited(e -> buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-border-radius: 5; -fx-background-radius: 5;"));
        buyButton.setDisable(true);
        accountComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                buyButton.setDisable(newVal == null || quantityField.getText().trim().isEmpty())
        );
        quantityField.textProperty().addListener((obs, oldVal, newVal) ->
                buyButton.setDisable(newVal.trim().isEmpty() || accountComboBox.getValue() == null)
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buyButtonType) {
                Map<String, Object> result = new HashMap<>();
                result.put("account", accountComboBox.getValue());
                try {
                    result.put("quantity", Double.parseDouble(quantityField.getText()));
                } catch (NumberFormatException e) {
                    showAlert("Ошибка", "Пожалуйста, введите корректное количество (например, 0.1).");
                    return null;
                }
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String selectedAccount = (String) result.get("account");
            double quantity = (Double) result.get("quantity");
            Account fromAccount = accounts.stream()
                    .filter(a -> a.getAccountNumber().equals(selectedAccount))
                    .findFirst()
                    .orElse(null);

            BigDecimal totalAmount = asset.getCurrentPrice().multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, BigDecimal.ROUND_HALF_UP); // Явное округление

            // Получаем комиссию для типа актива
            Commission assetCommission = commissionRepository.findByCommissionType(CommissionType.valueOf(currentMode))
                    .orElse(Commission.builder()
                            .amount(BigDecimal.ZERO)
                            .currency(asset.getCurrency())
                            .commissionDate(LocalDateTime.now())
                            .commissionType(CommissionType.valueOf(currentMode))
                            .percentage(BigDecimal.ZERO)
                            .isActive(true)
                            .build());
            BigDecimal assetCommissionAmount = assetCommission.getAmount()
                    .add(totalAmount.multiply(assetCommission.getPercentage().divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP); // Явное округление

            // Получаем комиссию для типа ПОКУПКА
            Commission purchaseCommission = commissionRepository.findByCommissionType(CommissionType.ПОКУПКА)
                    .orElse(Commission.builder()
                            .amount(BigDecimal.ZERO)
                            .currency(asset.getCurrency())
                            .commissionDate(LocalDateTime.now())
                            .commissionType(CommissionType.ПОКУПКА)
                            .percentage(BigDecimal.ZERO)
                            .isActive(true)
                            .build());
            BigDecimal purchaseCommissionAmount = purchaseCommission.getAmount()
                    .add(totalAmount.multiply(purchaseCommission.getPercentage().divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP); // Явное округление

            BigDecimal totalCommissionAmount = assetCommissionAmount.add(purchaseCommissionAmount)
                    .setScale(2, BigDecimal.ROUND_HALF_UP); // Явное округление
            BigDecimal totalAmountWithCommission = totalAmount.add(totalCommissionAmount)
                    .setScale(2, BigDecimal.ROUND_HALF_UP); // Явное округление

            Map<String, Double> balances = parseBalances(fromAccount.getBalances());
            double currentBalance = balances.getOrDefault(asset.getCurrency(), 0.0);
            if (BigDecimal.valueOf(currentBalance).compareTo(totalAmountWithCommission) < 0) {
                showAlert("Ошибка", "Недостаточно средств с учетом комиссии (" + totalCommissionAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + " " + asset.getCurrency() + ").");
                return;
            }

            Transaction transaction = Transaction.builder()
                    .user(currentUser)
                    .asset(asset)
                    .transactionType(Transaction.TransactionType.ПОКУПКА)
                    .amount(totalAmountWithCommission)
                    .currency(asset.getCurrency())
                    .quantity(quantity)
                    .transactionDate(LocalDateTime.now())
                    .fromAccount(fromAccount)
                    .build();

            try {
                transactionService.createTransaction(transaction);
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось выполнить транзакцию: " + e.getMessage());
                return;
            }

            try {
                Map<String, Double> portfolio = currentUser.getPortfolio() != null && !currentUser.getPortfolio().isEmpty()
                        ? objectMapper.readValue(currentUser.getPortfolio(), Map.class)
                        : new HashMap<>();
                portfolio.put(asset.getTicker(), portfolio.getOrDefault(asset.getTicker(), 0.0) + quantity);
                currentUser.setPortfolio(objectMapper.writeValueAsString(portfolio));
                userRepository.save(currentUser);
                showAlert("Успех", "Актив " + asset.getTicker() + " успешно куплен с использованием счета " + selectedAccount + "! Комиссия: " + totalCommissionAmount.setScale(2, BigDecimal.ROUND_HALF_UP) + " " + asset.getCurrency());
            } catch (JsonProcessingException e) {
                showAlert("Ошибка", "Не удалось обновить портфель: " + e.getMessage());
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 10; -fx-background-radius: 10;");
        alert.showAndWait();
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
            showAlert("Ошибка", "Ошибка парсинга баланса: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}