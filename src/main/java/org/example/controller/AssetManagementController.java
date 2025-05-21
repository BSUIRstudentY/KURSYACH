package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.example.entity.Asset;
import org.example.repository.AssetRepository;
import org.example.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AssetManagementController {

    @FXML
    private TableView<Asset> assetTable;

    @FXML
    private TableColumn<Asset, String> tickerColumn;

    @FXML
    private TableColumn<Asset, Asset.AssetType> assetTypeColumn;

    @FXML
    private TableColumn<Asset, String> nameColumn;

    @FXML
    private TableColumn<Asset, BigDecimal> currentPriceColumn;

    @FXML
    private TableColumn<Asset, String> currencyColumn;

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetRepository assetRepository;

    @FXML
    public void initialize() {
        tickerColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTicker()));
        assetTypeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAssetType()));
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        currentPriceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCurrentPrice()));
        currencyColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCurrency()));

        // Делаем поля редактируемыми с указанием StringConverter
        tickerColumn.setCellFactory(column -> new javafx.scene.control.cell.TextFieldTableCell<>(new DefaultStringConverter()));
        tickerColumn.setOnEditCommit(event -> {
            Asset asset = event.getRowValue();
            asset.setTicker(event.getNewValue());
        });

        assetTypeColumn.setCellFactory(column -> new javafx.scene.control.cell.ChoiceBoxTableCell<>(Asset.AssetType.values()));
        assetTypeColumn.setOnEditCommit(event -> {
            Asset asset = event.getRowValue();
            asset.setAssetType(event.getNewValue());
        });

        nameColumn.setCellFactory(column -> new javafx.scene.control.cell.TextFieldTableCell<>(new DefaultStringConverter()));
        nameColumn.setOnEditCommit(event -> {
            Asset asset = event.getRowValue();
            asset.setName(event.getNewValue());
        });

        currentPriceColumn.setCellFactory(column -> new javafx.scene.control.cell.TextFieldTableCell<>(new BigDecimalStringConverter()));
        currentPriceColumn.setOnEditCommit(event -> {
            Asset asset = event.getRowValue();
            asset.setCurrentPrice(event.getNewValue());
        });

        currencyColumn.setCellFactory(column -> new javafx.scene.control.cell.TextFieldTableCell<>(new DefaultStringConverter()));
        currencyColumn.setOnEditCommit(event -> {
            Asset asset = event.getRowValue();
            asset.setCurrency(event.getNewValue());
        });

        assetTable.setEditable(true);
        assetTable.setItems(FXCollections.observableArrayList(assetRepository.findAll()));
    }

    @FXML
    public void saveAsset() {
        for (Asset asset : assetTable.getItems()) {
            assetRepository.save(asset);
        }
        assetTable.refresh();
    }
}