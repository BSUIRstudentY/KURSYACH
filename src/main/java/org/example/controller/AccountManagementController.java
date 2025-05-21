package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import org.example.entity.Account;
import org.example.repository.AccountRepository;
import org.example.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountManagementController {

    @FXML
    private TableView<Account> accountTable;

    @FXML
    private TableColumn<Account, Integer> accountIdColumn;

    @FXML
    private TableColumn<Account, String> accountNumberColumn;

    @FXML
    private TableColumn<Account, String> nameColumn;

    @FXML
    private TableColumn<Account, String> balancesColumn;

    @FXML
    private TableColumn<Account, Account.AccountType> accountTypeColumn;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @FXML
    public void initialize() {
        // Define a StringConverter for String fields (pass-through)
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

        // Set up cell value factories
        accountIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAccountId()));
        accountNumberColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAccountNumber()));
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        balancesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBalances()));
        accountTypeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAccountType()));

        // Make fields editable with StringConverter
        accountNumberColumn.setCellFactory(column -> new TextFieldTableCell<>(stringConverter));
        accountNumberColumn.setOnEditCommit(event -> {
            Account account = event.getRowValue();
            account.setAccountNumber(event.getNewValue());
        });

        nameColumn.setCellFactory(column -> new TextFieldTableCell<>(stringConverter));
        nameColumn.setOnEditCommit(event -> {
            Account account = event.getRowValue();
            account.setName(event.getNewValue());
        });

        balancesColumn.setCellFactory(column -> new TextFieldTableCell<>(stringConverter));
        balancesColumn.setOnEditCommit(event -> {
            Account account = event.getRowValue();
            account.setBalances(event.getNewValue());
        });

        // No StringConverter needed for ChoiceBoxTableCell since it uses enum values directly
        accountTypeColumn.setCellFactory(column -> new javafx.scene.control.cell.ChoiceBoxTableCell<>(Account.AccountType.values()));
        accountTypeColumn.setOnEditCommit(event -> {
            Account account = event.getRowValue();
            account.setAccountType(event.getNewValue());
        });

        // Enable editing and load data
        accountTable.setEditable(true);
        accountTable.setItems(FXCollections.observableArrayList(accountRepository.findAll()));
    }

    @FXML
    public void saveAccount() {
        for (Account account : accountTable.getItems()) {
            accountRepository.save(account);
        }
        accountTable.refresh();
    }
}