package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.entity.Account;
import org.example.entity.Transaction;
import org.example.entity.User;
import org.example.repository.AccountRepository;
import org.example.service.AccountService;
import org.example.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.font.PdfEncodings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubscriptionsController {
    private static ConfigurableApplicationContext springContext;

    @FXML
    private ChoiceBox<String> accountChoiceBox;

    @FXML
    private ChoiceBox<String> periodChoiceBox;

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, String> dateColumn;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, String> amountColumn;

    @FXML
    private TableColumn<Transaction, String> detailsColumn;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    private User currentUser;
    private List<Account> userAccounts;

    @FXML
    public void initialize() {
        periodChoiceBox.getItems().addAll("Прошедшая неделя", "Прошедший месяц", "Прошедший год");
        periodChoiceBox.setValue("Прошедший месяц");
        periodChoiceBox.setOnAction(e -> updateTransactionTable());

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionType().toString()));
        amountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f %s", cellData.getValue().getAmount(), cellData.getValue().getCurrency())));
        detailsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(getTransactionDetails(cellData.getValue())));

        updateTransactionTable();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            userAccounts = accountRepository.findByUser(currentUser);
            accountChoiceBox.getItems().clear();
            accountChoiceBox.getItems().addAll(userAccounts.stream()
                    .map(Account::getAccountNumber)
                    .collect(Collectors.toList()));
            if (!userAccounts.isEmpty()) {
                accountChoiceBox.setValue(userAccounts.get(0).getAccountNumber());
            }
            updateTransactionTable();
        }
    }

    private void updateTransactionTable() {
        transactionTable.getItems().clear();

        if (currentUser == null || accountChoiceBox.getValue() == null) {
            return;
        }

        String selectedAccountNumber = accountChoiceBox.getValue();
        Account selectedAccount = accountRepository.findByUser(currentUser).stream()
                .filter(account -> account.getAccountNumber().equals(selectedAccountNumber))
                .findFirst()
                .orElse(null);

        if (selectedAccount == null) {
            return;
        }

        List<Transaction> transactions = transactionService.findByUser(currentUser).stream()
                .filter(t -> (t.getFromAccount() != null && t.getFromAccount().getAccountId().equals(selectedAccount.getAccountId())) ||
                        (t.getToAccount() != null && t.getToAccount().getAccountId().equals(selectedAccount.getAccountId())))
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        switch (periodChoiceBox.getValue()) {
            case "Прошедшая неделя":
                startDate = now.minusWeeks(1);
                break;
            case "Прошедший месяц":
                startDate = now.minusMonths(1);
                break;
            case "Прошедший год":
                startDate = now.minusYears(1);
                break;
            default:
                startDate = now.minusMonths(1);
        }

        transactions = transactions.stream()
                .filter(t -> t.getTransactionDate().isAfter(startDate) && t.getTransactionDate().isBefore(now))
                .collect(Collectors.toList());

        transactionTable.getItems().addAll(transactions);
    }

    @FXML
    public void exportToPDF() {
        if (currentUser == null || accountChoiceBox.getValue() == null) {
            showAlert("Выберите счёт для экспорта.");
            return;
        }

        String selectedAccountNumber = accountChoiceBox.getValue();
        Account selectedAccount = userAccounts.stream()
                .filter(account -> account.getAccountNumber().equals(selectedAccountNumber))
                .findFirst()
                .orElse(null);

        if (selectedAccount == null) {
            showAlert("Счёт не найден.");
            return;
        }

        List<Transaction> transactions = transactionTable.getItems();
        if (transactions.isEmpty()) {
            showAlert("Нет транзакций для экспорта.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить выписку в PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("statement_" + selectedAccountNumber + ".pdf");
        File file = fileChooser.showSaveDialog(new Stage());

        if (file == null) {
            showAlert("Сохранение отменено пользователем.");
            return;
        }

        try {
            generatePDF(selectedAccount, transactions, periodChoiceBox.getValue(), file);
            showAlert("Выписка успешно экспортирована в " + file.getAbsolutePath());
        } catch (Exception e) {
            showAlert("Ошибка при экспорте PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generatePDF(Account account, List<Transaction> transactions, String period, File outputFile) throws IOException {
        try {
            // Создаём PDF-документ
            PdfWriter writer = new PdfWriter(new FileOutputStream(outputFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40); // Увеличенные отступы для лучшего вида

            // Используем стандартный шрифт Times-Roman с поддержкой Unicode
            PdfFont font = PdfFontFactory.createFont("Times-Roman", "cp1251");
            PdfFont boldFont = PdfFontFactory.createFont("Times-Bold", "cp1251");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

            // Заголовок
            Paragraph title = new Paragraph("Выписка по счёту " + account.getAccountNumber())
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setFontColor(ColorConstants.BLACK)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(title);

            // Линия под заголовком
            com.itextpdf.kernel.pdf.canvas.draw.SolidLine line = new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f);
            line.setColor(ColorConstants.BLACK);
            document.add(new com.itextpdf.layout.element.LineSeparator(line).setMarginBottom(15));

            // Информация о периоде и дате
            Paragraph periodPara = new Paragraph("Период: " + period)
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(5);
            document.add(periodPara);

            Paragraph datePara = new Paragraph("Дата выписки: " + LocalDateTime.now().format(formatter))
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(20);
            document.add(datePara);

            // Создаём таблицу
            Table table = new Table(UnitValue.createPercentArray(new float[]{20, 20, 20, 40}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setFont(font);
            table.setFontSize(10);
            table.setBorder(new SolidBorder(ColorConstants.BLACK, 1));

            // Заголовки таблицы
            table.addHeaderCell(new Cell()
                    .add(new Paragraph("Дата").setFont(boldFont))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                    .setPadding(5));
            table.addHeaderCell(new Cell()
                    .add(new Paragraph("Тип").setFont(boldFont))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                    .setPadding(5));
            table.addHeaderCell(new Cell()
                    .add(new Paragraph("Сумма").setFont(boldFont))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                    .setPadding(5));
            table.addHeaderCell(new Cell()
                    .add(new Paragraph("Детали").setFont(boldFont))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                    .setPadding(5));

            // Заполняем таблицу данными
            for (Transaction transaction : transactions) {
                table.addCell(new Cell()
                        .add(new Paragraph(transaction.getTransactionDate().format(formatter)))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                        .setPadding(5));
                table.addCell(new Cell()
                        .add(new Paragraph(transaction.getTransactionType().toString()))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                        .setPadding(5));
                table.addCell(new Cell()
                        .add(new Paragraph(String.format("%.2f %s", transaction.getAmount(), transaction.getCurrency())))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                        .setPadding(5));
                table.addCell(new Cell()
                        .add(new Paragraph(getTransactionDetails(transaction)))
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                        .setPadding(5));
            }

            document.add(table);

            // Добавляем подвал с номером страницы
            int numberOfPages = pdf.getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                PdfPage page = pdf.getPage(i);
                PdfCanvas canvas = new PdfCanvas(page);
                canvas.beginText()
                        .setFontAndSize(font, 10)
                        .moveText(500, 30)
                        .showText("Страница " + i + " из " + numberOfPages)
                        .endText();
                canvas.release();
            }

            document.close();
        } catch (Exception e) {
            throw new IOException("Ошибка при создании PDF: " + e.getMessage() + " (Попробуйте использовать внешний шрифт, например Noto Sans, если проблема сохраняется.)", e);
        }
    }

    private String getTransactionDetails(Transaction transaction) {
        switch (transaction.getTransactionType()) {
            case ПОКУПКА:
            case ПРОДАЖА:
                return String.format("Актив: %s, Количество: %.2f", transaction.getAsset().getTicker(), transaction.getQuantity());
            case ПОПОЛНЕНИЕ:
            case СНЯТИЕ:
                return String.format("Сумма: %.2f %s", transaction.getAmount(), transaction.getCurrency());
            case ОБМЕН:
                return String.format("Обмен: %s → %s", transaction.getCurrency(), transaction.getToCurrency() != null ? transaction.getToCurrency() : "Не указана");
            default:
                return "Неизвестная операция";
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}