package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import org.example.entity.Commission;
import org.example.entity.CommissionType;
import org.example.repository.CommissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommissionManagementController {

    @FXML
    private TableView<Commission> commissionTable;

    @FXML
    private TableColumn<Commission, CommissionType> typeColumn;

    @FXML
    private TableColumn<Commission, BigDecimal> amountColumn;

    @FXML
    private TableColumn<Commission, String> currencyColumn;

    @FXML
    private TableColumn<Commission, BigDecimal> percentageColumn;

    @FXML
    private TableColumn<Commission, Boolean> activeColumn;

    private final CommissionRepository commissionRepository;

    @Autowired
    public CommissionManagementController(CommissionRepository commissionRepository) {
        this.commissionRepository = commissionRepository;
    }

    @FXML
    public void initialize() {
        // StringConverter for String fields (currencyColumn)
        StringConverter<String> stringConverter = new StringConverter<>() {
            @Override
            public String toString(String object) {
                return object == null ? "" : object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        };

        // StringConverter for BigDecimal fields
        StringConverter<BigDecimal> bigDecimalConverter = new StringConverter<>() {
            @Override
            public String toString(BigDecimal value) {
                return value == null ? "" : value.toString();
            }

            @Override
            public BigDecimal fromString(String string) {
                try {
                    return string.isEmpty() ? BigDecimal.ZERO : new BigDecimal(string);
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO; // Default to 0 for invalid input
                }
            }
        };

        // Set up cell value factories
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCommissionType()));
        amountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
        currencyColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCurrency()));
        percentageColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPercentage()));
        activeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isActive()));

        // Make fields editable
        typeColumn.setCellFactory(column -> new javafx.scene.control.cell.ChoiceBoxTableCell<>(CommissionType.values()));
        typeColumn.setOnEditCommit(event -> {
            Commission commission = event.getRowValue();
            commission.setCommissionType(event.getNewValue());
        });

        amountColumn.setCellFactory(column -> new TextFieldTableCell<>(bigDecimalConverter));
        amountColumn.setOnEditCommit(event -> {
            Commission commission = event.getRowValue();
            commission.setAmount(event.getNewValue());
        });

        currencyColumn.setCellFactory(column -> new TextFieldTableCell<>(stringConverter));
        currencyColumn.setOnEditCommit(event -> {
            Commission commission = event.getRowValue();
            commission.setCurrency(event.getNewValue());
        });

        percentageColumn.setCellFactory(column -> new TextFieldTableCell<>(bigDecimalConverter));
        percentageColumn.setOnEditCommit(event -> {
            Commission commission = event.getRowValue();
            commission.setPercentage(event.getNewValue());
        });

        activeColumn.setCellFactory(column -> new CheckBoxTableCell<>());
        activeColumn.setOnEditCommit(event -> {
            Commission commission = event.getRowValue();
            commission.setActive(event.getNewValue());
        });

        // Populate table with all CommissionType enums
        List<Commission> commissions = Arrays.stream(CommissionType.values())
                .map(type -> commissionRepository.findByCommissionType(type)
                        .orElseGet(() -> Commission.builder()
                                .commissionType(type)
                                .amount(BigDecimal.ZERO)
                                .currency("USD")
                                .commissionDate(LocalDateTime.now())
                                .percentage(BigDecimal.ZERO)
                                .isActive(true)
                                .build()))
                .collect(Collectors.toList());

        commissionTable.setEditable(true);
        commissionTable.setItems(FXCollections.observableArrayList(commissions));
    }

    @FXML
    public void saveCommission() {
        for (Commission commission : commissionTable.getItems()) {
            commissionRepository.save(commission);
        }
        commissionTable.refresh();
    }
}