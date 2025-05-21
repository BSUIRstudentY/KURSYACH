package org.example.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
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
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;

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

    @FXML
    public void initialize() {
        loadBasicMetricsPieChart();
        loadTransactionVolumeTrendChart();
        loadUserRoleDistributionChart();
        loadUserActivityBarChart();
        loadAccountTypePieChart();
        loadAssetPriceScatterChart();
        loadTransactionTrendChart();
        loadTransactionTypeVolumeChart();
        loadCommissionAmountChart();
        loadCommissionTrendChart();
        loadNotificationTrendChart();
        loadUnreadNotificationBarChart();
        loadAssetTypeDistributionChart();
        loadAssetPriceTrendChart();
    }

    private void loadBasicMetricsPieChart() {
        Map<String, Long> activityDistribution = new HashMap<>();
        activityDistribution.put("Активные пользователи", userRepository.count());
        activityDistribution.put("Неактивные пользователи", 0L); // Пример, нужно адаптировать под данные
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

    @FXML
    public void exportCharts() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить графики");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        Map<String, Chart> charts = new HashMap<>();
        charts.put("basicMetrics", basicMetricsPieChart);
        charts.put("transactionVolumeTrend", transactionVolumeTrendChart);
        charts.put("userRoleDistribution", userRoleDistributionChart);
        charts.put("userActivity", userActivityBarChart);
        charts.put("accountType", accountTypePieChart);
        charts.put("assetPriceScatter", assetPriceScatterChart);
        charts.put("transactionTrend", transactionTrendChart);
        charts.put("transactionTypeVolume", transactionTypeVolumeChart);
        charts.put("commissionAmount", commissionAmountChart);
        charts.put("commissionTrend", commissionTrendChart);
        charts.put("notificationTrend", notificationTrendChart);
        charts.put("unreadNotification", unreadNotificationBarChart);
        charts.put("assetTypeDistribution", assetTypeDistributionChart);
        charts.put("assetPriceTrend", assetPriceTrendChart);

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

                document.add(new Paragraph("Полный отчет по статистике", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                document.add(new Paragraph("Дата генерации: " + LocalDateTime.now().format(dateTimeFormatter)));
                document.add(new Paragraph(" "));

                document.add(new Paragraph("Графики и данные", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                document.add(new Paragraph("Все данные представлены в графическом виде на вкладках интерфейса."));
                document.add(new Paragraph(" "));

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
                writer.write("Полный отчет по данным системы\n");
                writer.write("Дата генерации: " + LocalDateTime.now().format(dateTimeFormatter) + "\n\n");
                writer.write("Все данные представлены в графическом виде на вкладках интерфейса.\n");
                statusArea.appendText("Данные экспортированы в " + file.getName() + "\n");
            } catch (IOException e) {
                statusArea.appendText("Ошибка при экспорте данных: " + e.getMessage() + "\n");
            }
        }
    }
}