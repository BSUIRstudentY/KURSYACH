package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.entity.Message;
import org.example.entity.Notification;
import org.example.entity.User;
import org.example.repository.MessageRepository;
import org.example.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AdminSupportController {
    private static ConfigurableApplicationContext springContext;

    @FXML
    private TableView<Message> supportTable;

    @FXML
    private TableColumn<Message, Void> actionsColumn;

    @FXML
    private TableView<Message> dialogTable;

    @FXML
    private TextArea replyTextArea;

    @FXML
    private Button sendReplyButton;

    @FXML
    private Button refreshButton;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User currentAdmin;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        setupSupportTableColumns();
        setupDialogTableColumns();
        refreshSupportTable();
        setupButtonStates();
    }

    @SuppressWarnings("unchecked")
    private void setupSupportTableColumns() {
        TableColumn<Message, LocalDateTime> timestampColumn = (TableColumn<Message, LocalDateTime>) supportTable.getColumns().get(4);
        timestampColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimestamp()));
        timestampColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        TableColumn<Message, Boolean> resolvedColumn = (TableColumn<Message, Boolean>) supportTable.getColumns().get(5);
        resolvedColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().isResolved()));
        resolvedColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Да" : "Нет"));
            }
        });

        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Message, Void> call(final TableColumn<Message, Void> param) {
                return new TableCell<>() {
                    private final Button claimButton = new Button("Занять");
                    private final Button resolveButton = new Button("Завершить");

                    {
                        claimButton.setStyle("-fx-background-color: #6a11cb; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
                        resolveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");

                        claimButton.setOnAction(e -> {
                            Message message = getTableView().getItems().get(getIndex());
                            if (message.getReceiver() == null) {
                                message.setReceiver(currentAdmin);
                                messageRepository.save(message);
                                sendNotification(Long.valueOf(message.getSender().getUserId()), "Ваш вопрос '" + message.getTitle() + "' взят в работу админом " + currentAdmin.getFirstName());
                                refreshSupportTable();
                            } else {
                                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Этот вопрос уже закреплен за другим админом!");
                            }
                        });

                        resolveButton.setOnAction(e -> {
                            Message message = getTableView().getItems().get(getIndex());
                            if (message.getReceiver() != null && message.getReceiver().equals(currentAdmin)) {
                                message.setResolved(true);
                                messageRepository.save(message);
                                sendNotification(Long.valueOf(message.getSender().getUserId()), "Ваш вопрос '" + message.getTitle() + "' разрешен.");
                                refreshSupportTable();
                            } else {
                                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Вы не можете завершить этот вопрос, так как он не закреплен за вами!");
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Message message = getTableView().getItems().get(getIndex());
                            HBox buttons = new HBox(10);
                            if (message.getReceiver() == null) {
                                buttons.getChildren().add(claimButton);
                            } else if (message.getReceiver().equals(currentAdmin)) {
                                buttons.getChildren().add(resolveButton);
                            }
                            setGraphic(buttons);
                        }
                    }
                };
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setupDialogTableColumns() {
        TableColumn<Message, LocalDateTime> timestampColumn = (TableColumn<Message, LocalDateTime>) dialogTable.getColumns().get(2);
        timestampColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimestamp()));
        timestampColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        TableColumn<Message, String> contentColumn = (TableColumn<Message, String>) dialogTable.getColumns().get(1);
        contentColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                } else {
                    Message message = getTableView().getItems().get(getIndex());
                    try {
                        setText(message.getMessageDetails().getSenderMessage());
                    } catch (Exception e) {
                        setText("Ошибка парсинга: " + e.getMessage());
                    }
                }
            }
        });
    }

    @FXML
    public void refreshSupportTable() {
        // Получаем все неразрешенные начальные сообщения, независимо от receiver
        List<Message> messages = messageRepository.findByIsResolvedFalseAndParentMessageIsNull();
        supportTable.getItems().setAll(messages);
        setupButtonStates();
        if (supportTable.getSelectionModel().getSelectedItem() == null) {
            dialogTable.getItems().clear();
        }
    }

    @FXML
    public void sendReply() {
        Message selectedMessage = supportTable.getSelectionModel().getSelectedItem();
        if (selectedMessage == null) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите вопрос для ответа!");
            return;
        }

        String replyContent = replyTextArea.getText().trim();
        if (replyContent.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Введите текст ответа!");
            return;
        }

        try {
            // Создаем ответное сообщение
            Message replyMessage = Message.builder()
                    .sender(currentAdmin)
                    .receiver(selectedMessage.getSender())
                    .title("Ответ на: " + selectedMessage.getTitle())
                    .content(replyContent)
                    .timestamp(LocalDateTime.now())
                    .isResolved(false)
                    .parentMessage(selectedMessage)
                    .dialogId(selectedMessage.getDialogId())
                    .build();
            messageRepository.save(replyMessage);

            sendNotification(Long.valueOf(selectedMessage.getSender().getUserId()), "Вы получили ответ на вопрос '" + selectedMessage.getTitle() + "': " + replyContent);
            replyTextArea.clear();
            loadDialog(selectedMessage.getDialogId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при отправке ответа: " + e.getMessage());
        }
    }

    private void loadDialog(Long dialogId) {
        if (dialogId != null) {
            // Загружаем все сообщения для данного dialogId
            List<Message> dialogMessages = messageRepository.findByDialogIdOrderByTimestampAsc(dialogId);
            dialogTable.getItems().setAll(dialogMessages);
        } else {
            dialogTable.getItems().clear();
        }
    }

    private void setupButtonStates() {
        sendReplyButton.setDisable(supportTable.getSelectionModel().getSelectedItem() == null);
        replyTextArea.setDisable(supportTable.getSelectionModel().getSelectedItem() == null);
        supportTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            sendReplyButton.setDisable(newSelection == null);
            replyTextArea.setDisable(newSelection == null);
            if (newSelection != null) {
                loadDialog(newSelection.getDialogId());
            } else {
                dialogTable.getItems().clear();
            }
        });
    }

    private void sendNotification(Long userId, String content) {
        Notification notification = Notification.builder()
                .userId(userId)
                .content(content)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setCurrentAdmin(User admin) {
        this.currentAdmin = admin;
    }

    public static void setSpringContext(ConfigurableApplicationContext context) {
        springContext = context;
    }
}