package org.example.controller;

import jakarta.transaction.Transactional;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.converter.DefaultStringConverter;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserManagementController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> userIdColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> firstNameColumn;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, User.Role> roleColumn;



    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @FXML
    public void initialize() {
        userIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getUserId()));
        emailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        firstNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));
        roleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRole()));


        // Делаем поля редактируемыми с указанием StringConverter
        emailColumn.setCellFactory(column -> new javafx.scene.control.cell.TextFieldTableCell<>(new DefaultStringConverter()));
        emailColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setEmail(event.getNewValue());
        });

        firstNameColumn.setCellFactory(column -> new javafx.scene.control.cell.TextFieldTableCell<>(new DefaultStringConverter()));
        firstNameColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setFirstName(event.getNewValue());
        });

        lastNameColumn.setCellFactory(column -> new javafx.scene.control.cell.TextFieldTableCell<>(new DefaultStringConverter()));
        lastNameColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setLastName(event.getNewValue());
        });

        roleColumn.setCellFactory(column -> new javafx.scene.control.cell.ChoiceBoxTableCell<>(User.Role.values()));
        roleColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setRole(event.getNewValue());
        });



        userTable.setEditable(true);
        userTable.setItems(FXCollections.observableArrayList(userRepository.findAll()));
    }

    @FXML
    public void saveUser() {
        for (User user : userTable.getItems()) {
            userRepository.save(user);
        }
        userTable.refresh();
    }
}