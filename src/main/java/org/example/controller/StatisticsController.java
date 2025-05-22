package org.example.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.example.entity.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

@Component
public class StatisticsController {

    @FXML
    private TabPane tabPane;

    @FXML
    private PieChart basicMetricsPieChart;

    @FXML
    private LineChart<String, Number> transactionVolumeTrendChart;

    @FXML
    private PieChart userRoleDistributionChart;

    @FXML
    private BarChart<String, Number> userActivityBarChart;

    @FXML
    private PieChart accountTypePieChart;

    @FXML
    private ScatterChart<String, Number> assetPriceScatterChart;

    @FXML
    private LineChart<String, Number> transactionTrendChart;

    @FXML
    private StackedBarChart<String, Number> transactionTypeVolumeChart;

    @FXML
    private StackedBarChart<String, Number> commissionAmountChart;

    @FXML
    private AreaChart<String, Number> commissionTrendChart;

    @FXML
    private AreaChart<String, Number> notificationTrendChart;

    @FXML
    private BarChart<String, Number> unreadNotificationBarChart;

    @FXML
    private PieChart assetTypeDistributionChart;

    @FXML
    private ScatterChart<String, Number> assetPriceTrendChart;

    @FXML
    private Button exportChartsButton;

    @FXML
    private Button exportCurrentChartButton;

    @FXML
    private Button exportReportButton;

    @FXML
    private Button exportDataButton;

    @FXML
    private TextArea statusArea;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private User currentUser;

    @FXML
    public void initialize() {
        if (basicMetricsPieChart == null) System.out.println("basicMetricsPieChart is null");
        if (transactionVolumeTrendChart == null) System.out.println("transactionVolumeTrendChart is null");
        if (userRoleDistributionChart == null) System.out.println("userRoleDistributionChart is null");
        if (userActivityBarChart == null) System.out.println("userActivityBarChart is null");
        if (accountTypePieChart == null) System.out.println("accountTypePieChart is null");
        if (assetPriceScatterChart == null) System.out.println("assetPriceScatterChart is null");
        if (transactionTrendChart == null) System.out.println("transactionTrendChart is null");
        if (transactionTypeVolumeChart == null) System.out.println("transactionTypeVolumeChart is null");
        if (commissionAmountChart == null) System.out.println("commissionAmountChart is null");
        if (commissionTrendChart == null) System.out.println("commissionTrendChart is null");
        if (notificationTrendChart == null) System.out.println("notificationTrendChart is null");
        if (unreadNotificationBarChart == null) System.out.println("unreadNotificationBarChart is null");
        if (assetTypeDistributionChart == null) System.out.println("assetTypeDistributionChart is null");
        if (assetPriceTrendChart == null) System.out.println("assetPriceTrendChart is null");

        if (basicMetricsPieChart != null) loadBasicMetricsPieChart();
        if (transactionVolumeTrendChart != null) loadTransactionVolumeTrendChart();
        if (userRoleDistributionChart != null) loadUserRoleDistributionChart();
        if (userActivityBarChart != null) loadUserActivityBarChart();
        if (accountTypePieChart != null) loadAccountTypePieChart();
        if (assetPriceScatterChart != null) loadAssetPriceScatterChart();
        if (transactionTrendChart != null) loadTransactionTrendChart();
        if (transactionTypeVolumeChart != null) loadTransactionTypeVolumeChart();
        if (commissionAmountChart != null) loadCommissionAmountChart();
        if (commissionTrendChart != null) loadCommissionTrendChart();
        if (notificationTrendChart != null) loadNotificationTrendChart();
        if (unreadNotificationBarChart != null) loadUnreadNotificationBarChart();
        if (assetTypeDistributionChart != null) loadAssetTypeDistributionChart();
        if (assetPriceTrendChart != null) loadAssetPriceTrendChart();
    }

    private void loadBasicMetricsPieChart() {
        Map<String, Long> activityDistribution = new HashMap<>();
        activityDistribution.put("Активные пользователи", userRepository.count());
        activityDistribution.put("Неактивные пользователи", 0L);
        basicMetricsPieChart.getData().clear();
        activityDistribution.forEach((key, value) -> basicMetricsPieChart.getData().add(new PieChart.Data(key, value)));
    }

    private void loadTransactionVolumeTrendChart() {
        Map<String, BigDecimal> volumeTrend = transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().format(monthFormatter),
                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Объем транзакций");
        volumeTrend.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue().doubleValue())));
        transactionVolumeTrendChart.getData().setAll(series);
    }

    private void loadUserRoleDistributionChart() {
        Map<String, Long> roleDistribution = userRepository.findAll().stream()
                .collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()));
        userRoleDistributionChart.getData().clear();
        roleDistribution.forEach((role, count) -> userRoleDistributionChart.getData().add(new PieChart.Data(role, count)));
    }

    private void loadUserActivityBarChart() {
        Map<String, Long> activityByRole = transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        t -> t.getUser().getRole().name(),
                        Collectors.counting()
                ));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Количество транзакций");
        activityByRole.forEach((role, count) -> series.getData().add(new XYChart.Data<>(role, count)));
        userActivityBarChart.getData().setAll(series);
    }

    private void loadAccountTypePieChart() {
        Map<String, Long> typeDistribution = accountRepository.findAll().stream()
                .collect(Collectors.groupingBy(a -> a.getAccountType().name(), Collectors.counting()));
        accountTypePieChart.getData().clear();
        typeDistribution.forEach((type, count) -> accountTypePieChart.getData().add(new PieChart.Data(type, count)));
    }

    private void loadAssetPriceScatterChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Цены активов");
        assetRepository.findAll().forEach(a -> series.getData().add(new XYChart.Data<>(a.getName(), a.getCurrentPrice().doubleValue())));
        assetPriceScatterChart.getData().setAll(series);
    }

    private void loadTransactionTrendChart() {
        Map<String, Long> trend = transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(t -> t.getTransactionDate().format(monthFormatter), Collectors.counting()));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Количество транзакций");
        trend.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        transactionTrendChart.getData().setAll(series);
    }

    private void loadTransactionTypeVolumeChart() {
        Map<String, BigDecimal> volume = transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionType().name(),
                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Объем по типам");
        volume.forEach((type, amount) -> series.getData().add(new XYChart.Data<>(type, amount.doubleValue())));
        transactionTypeVolumeChart.getData().setAll(series);
    }

    private void loadCommissionAmountChart() {
        Map<String, BigDecimal> commissionAmounts = commissionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCommissionType().name(),
                        Collectors.mapping(Commission::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Сумма комиссий");
        commissionAmounts.forEach((type, amount) -> series.getData().add(new XYChart.Data<>(type, amount.doubleValue())));
        commissionAmountChart.getData().setAll(series);
    }

    private void loadCommissionTrendChart() {
        Map<String, Long> trend = commissionRepository.findAll().stream()
                .collect(Collectors.groupingBy(c -> c.getCommissionDate().format(dayFormatter), Collectors.counting()));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Активные комиссии");
        trend.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        commissionTrendChart.getData().setAll(series);
    }

    private void loadNotificationTrendChart() {
        Map<String, Long> trend = notificationRepository.findAll().stream()
                .collect(Collectors.groupingBy(n -> n.getTimestamp().format(dayFormatter), Collectors.counting()));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Уведомления");
        trend.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        notificationTrendChart.getData().setAll(series);
    }

    private void loadUnreadNotificationBarChart() {
        Map<String, Long> statusDistribution = notificationRepository.findAll().stream()
                .collect(Collectors.groupingBy(n -> n.isRead() ? "Прочитано" : "Непрочитано", Collectors.counting()));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Статус уведомлений");
        statusDistribution.forEach((status, count) -> series.getData().add(new XYChart.Data<>(status, count)));
        unreadNotificationBarChart.getData().setAll(series);
    }

    private void loadAssetTypeDistributionChart() {
        Map<String, Long> typeDistribution = assetRepository.findAll().stream()
                .collect(Collectors.groupingBy(a -> a.getAssetType().name(), Collectors.counting()));
        assetTypeDistributionChart.getData().clear();
        typeDistribution.forEach((type, count) -> assetTypeDistributionChart.getData().add(new PieChart.Data(type, count)));
    }

    private void loadAssetPriceTrendChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Тренд цен активов");
        assetRepository.findAll().forEach(a -> series.getData().add(new XYChart.Data<>(a.getName(), a.getCurrentPrice().doubleValue())));
        assetPriceTrendChart.getData().setAll(series);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        if (statusArea != null) {
            statusArea.appendText("Текущий пользователь: " + (currentUser != null ? currentUser.getFirstName() : "не установлен") + "\n");
        }
    }

    @FXML
    public void exportCurrentChart() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить текущий график");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            VBox content = (VBox) selectedTab.getContent();
            Chart chart = null;
            for (javafx.scene.Node node : content.getChildren()) {
                if (node instanceof Chart) {
                    chart = (Chart) node;
                    break;
                }
            }
            if (chart != null) {
                File file = fileChooser.showSaveDialog(null);
                if (file != null) {
                    try {
                        WritableImage writableImage = chart.snapshot(null, null);
                        int width = (int) writableImage.getWidth();
                        int height = (int) writableImage.getHeight();
                        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        PixelReader pixelReader = writableImage.getPixelReader();
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                int argb = pixelReader.getArgb(x, y);
                                bufferedImage.setRGB(x, y, argb);
                            }
                        }
                        ImageIO.write(bufferedImage, "png", file);
                        statusArea.appendText("Текущий график сохранен как " + file.getName() + "\n");
                    } catch (IOException e) {
                        statusArea.appendText("Ошибка при сохранении текущего графика: " + e.getMessage() + "\n");
                    }
                }
            } else {
                statusArea.appendText("Текущая вкладка не содержит график\n");
            }
        }
    }

    @FXML
    public void exportCharts() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить графики");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            VBox content = (VBox) selectedTab.getContent();
            Map<String, Chart> charts = new HashMap<>();
            int chartIndex = 1;
            for (javafx.scene.Node node : content.getChildren()) {
                if (node instanceof Chart chart) {
                    charts.put(selectedTab.getText() + "_Chart" + chartIndex++, chart);
                }
            }

            if (charts.isEmpty()) {
                statusArea.appendText("Текущая вкладка не содержит графиков для экспорта\n");
                return;
            }

            for (Map.Entry<String, Chart> entry : charts.entrySet()) {
                File file = fileChooser.showSaveDialog(null);
                if (file != null) {
                    try {
                        WritableImage writableImage = entry.getValue().snapshot(null, null);
                        int width = (int) writableImage.getWidth();
                        int height = (int) writableImage.getHeight();
                        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        PixelReader pixelReader = writableImage.getPixelReader();
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                int argb = pixelReader.getArgb(x, y);
                                bufferedImage.setRGB(x, y, argb);
                            }
                        }
                        ImageIO.write(bufferedImage, "png", file);
                        statusArea.appendText("График " + entry.getKey() + " сохранен как " + file.getName() + "\n");
                    } catch (IOException e) {
                        statusArea.appendText("Ошибка при сохранении " + entry.getKey() + ": " + e.getMessage() + "\n");
                    }
                }
            }
        } else {
            statusArea.appendText("Выберите вкладку для экспорта графиков\n");
        }
    }

    @FXML
    public void exportReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                document.add(new Paragraph("Отчет по статистике: " + tabPane.getSelectionModel().getSelectedItem().getText(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                document.add(new Paragraph("Дата генерации: " + LocalDateTime.now().format(dateTimeFormatter)));
                document.add(new Paragraph(" "));

                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                if (selectedTab != null) {
                    VBox content = (VBox) selectedTab.getContent();
                    Map<String, Chart> charts = new HashMap<>();
                    int chartIndex = 1;
                    for (javafx.scene.Node node : content.getChildren()) {
                        if (node instanceof Chart chart) {
                            charts.put(selectedTab.getText() + "_Chart" + chartIndex++, chart);
                        }
                    }

                    if (charts.isEmpty()) {
                        document.add(new Paragraph("На текущей вкладке нет графиков"));
                    } else {
                        for (Map.Entry<String, Chart> entry : charts.entrySet()) {
                            WritableImage writableImage = entry.getValue().snapshot(null, null);
                            int width = (int) writableImage.getWidth();
                            int height = (int) writableImage.getHeight();
                            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                            PixelReader pixelReader = writableImage.getPixelReader();
                            for (int y = 0; y < height; y++) {
                                for (int x = 0; x < width; x++) {
                                    int argb = pixelReader.getArgb(x, y);
                                    bufferedImage.setRGB(x, y, argb);
                                }
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(bufferedImage, "png", baos);
                            Image pdfImage = Image.getInstance(baos.toByteArray());
                            pdfImage.scaleToFit(500, 300);
                            document.add(new Paragraph(entry.getKey(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                            document.add(pdfImage);
                            document.add(new Paragraph(" "));
                        }
                    }
                } else {
                    document.add(new Paragraph("Выберите вкладку для экспорта"));
                }

                document.close();
                statusArea.appendText("Отчет сохранен как " + file.getName() + "\n");
            } catch (DocumentException | IOException e) {
                statusArea.appendText("Ошибка при сохранении отчета: " + e.getMessage() + "\n");
            }
        }
    }

    @FXML
    public void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить данные");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                if (selectedTab == null) {
                    writer.write("Выберите вкладку для экспорта данных\n");
                    statusArea.appendText("Выберите вкладку для экспорта данных\n");
                    return;
                }

                String tabTitle = selectedTab.getText();
                writer.write("Отчет по данным: " + tabTitle + "\n");
                writer.write("Дата генерации: " + LocalDateTime.now().format(dateTimeFormatter) + "\n\n");

                switch (tabTitle) {
                    case "Базовые показатели":
                        writer.write("=== Пользователи ===\n");
                        writer.write("Общее количество пользователей: " + userRepository.count() + "\n");
                        writer.write("Активные пользователи: " + userRepository.count() + "\n");
                        writer.write("Неактивные пользователи: 0\n");

                        writer.write("\n=== Транзакции (объем по месяцам) ===\n");
                        Map<String, BigDecimal> volumeTrend = transactionRepository.findAll().stream()
                                .collect(Collectors.groupingBy(
                                        t -> t.getTransactionDate().format(monthFormatter),
                                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                                ));
                        volumeTrend.entrySet().stream().sorted(Map.Entry.comparingByKey())
                                .forEach(e -> {
                                    try {
                                        writer.write("Месяц " + e.getKey() + ": " + e.getValue() + " валютных единиц\n");
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                });
                        break;

                    case "Распределение ролей пользователей":
                        writer.write("=== Распределение ролей пользователей ===\n");
                        Map<String, Long> roleDistribution = userRepository.findAll().stream()
                                .collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()));
                        roleDistribution.forEach((role, count) -> {
                            try {
                                writer.write("Роль " + role + ": " + count + " пользователей\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        writer.write("\n=== Активность пользователей по ролям ===\n");
                        Map<String, Long> activityByRole = transactionRepository.findAll().stream()
                                .collect(Collectors.groupingBy(
                                        t -> t.getUser().getRole().name(),
                                        Collectors.counting()
                                ));
                        activityByRole.forEach((role, count) -> {
                            try {
                                writer.write("Роль " + role + ": " + count + " транзакций\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;

                    case "Роли пользователей":
                        writer.write("=== Распределение типов счетов ===\n");
                        Map<String, Long> accountTypeDistribution = accountRepository.findAll().stream()
                                .collect(Collectors.groupingBy(a -> a.getAccountType().name(), Collectors.counting()));
                        accountTypeDistribution.forEach((type, count) -> {
                            try {
                                writer.write("Тип аккаунта " + type + ": " + count + " аккаунтов\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        writer.write("\n=== Цены активов ===\n");
                        assetRepository.findAll().forEach(a -> {
                            try {
                                writer.write("Актив " + a.getName() + ": " + a.getCurrentPrice() + "\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;

                    case "Транзакции":
                        writer.write("=== Тренд транзакций ===\n");
                        Map<String, Long> trend = transactionRepository.findAll().stream()
                                .collect(Collectors.groupingBy(t -> t.getTransactionDate().format(monthFormatter), Collectors.counting()));
                        trend.entrySet().stream().sorted(Map.Entry.comparingByKey())
                                .forEach(e -> {
                                    try {
                                        writer.write("Месяц " + e.getKey() + ": " + e.getValue() + " транзакций\n");
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                });

                        writer.write("\n=== Объем транзакций по типам ===\n");
                        Map<String, BigDecimal> transactionVolume = transactionRepository.findAll().stream()
                                .collect(Collectors.groupingBy(
                                        t -> t.getTransactionType().name(),
                                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                                ));
                        transactionVolume.forEach((type, amount) -> {
                            try {
                                writer.write("Тип транзакции " + type + ": " + amount + " валютных единиц\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;

                    case "Комиссии":
                        writer.write("=== Сумма комиссий по типам ===\n");
                        Map<String, BigDecimal> commissionAmounts = commissionRepository.findAll().stream()
                                .collect(Collectors.groupingBy(
                                        c -> c.getCommissionType().name(),
                                        Collectors.mapping(Commission::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                                ));
                        commissionAmounts.forEach((type, amount) -> {
                            try {
                                writer.write("Тип комиссии " + type + ": " + amount + " валютных единиц\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        writer.write("\n=== Тренд активных комиссий ===\n");
                        Map<String, Long> commissionTrend = commissionRepository.findAll().stream()
                                .collect(Collectors.groupingBy(c -> c.getCommissionDate().format(dayFormatter), Collectors.counting()));
                        commissionTrend.entrySet().stream().sorted(Map.Entry.comparingByKey())
                                .forEach(e -> {
                                    try {
                                        writer.write("Дата " + e.getKey() + ": " + e.getValue() + " активных комиссий\n");
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                });
                        break;

                    case "Уведомления":
                        writer.write("=== Тренд уведомлений ===\n");
                        Map<String, Long> notificationTrend = notificationRepository.findAll().stream()
                                .collect(Collectors.groupingBy(n -> n.getTimestamp().format(dayFormatter), Collectors.counting()));
                        notificationTrend.entrySet().stream().sorted(Map.Entry.comparingByKey())
                                .forEach(e -> {
                                    try {
                                        writer.write("День " + e.getKey() + ": " + e.getValue() + " уведомлений\n");
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                });

                        writer.write("\n=== Статус уведомлений ===\n");
                        Map<String, Long> notificationStatus = notificationRepository.findAll().stream()
                                .collect(Collectors.groupingBy(n -> n.isRead() ? "Прочитано" : "Непрочитано", Collectors.counting()));
                        notificationStatus.forEach((status, count) -> {
                            try {
                                writer.write("Статус " + status + ": " + count + " уведомлений\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;

                    case "Активы":
                        writer.write("=== Распределение типов активов ===\n");
                        Map<String, Long> assetTypeDistribution = assetRepository.findAll().stream()
                                .collect(Collectors.groupingBy(a -> a.getAssetType().name(), Collectors.counting()));
                        assetTypeDistribution.forEach((type, count) -> {
                            try {
                                writer.write("Тип актива " + type + ": " + count + " активов\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        writer.write("\n=== Тренд цен активов ===\n");
                        assetRepository.findAll().forEach(a -> {
                            try {
                                writer.write("Актив " + a.getName() + ": " + a.getCurrentPrice() + "\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;

                    default:
                        writer.write("Данные для этой вкладки не поддерживаются\n");
                }

                statusArea.appendText("Данные экспортированы в " + file.getName() + "\n");
            } catch (IOException e) {
                statusArea.appendText("Ошибка при экспорте данных: " + e.getMessage() + "\n");
            }
        }
    }
}