package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.entity.Message;
import org.example.entity.User;
import org.example.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class SupportController {
    private static ConfigurableApplicationContext springContext;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea contentArea;

    @FXML
    private ListView<String> questionList;

    @FXML
    private ListView<String> dialogList;

    @FXML
    private TextArea replyArea;

    @FXML
    private Button refreshDialogButton;

    @FXML
    private Button sendReplyButton;

    @Autowired
    private MessageService messageService;

    private User currentUser;
    private Timer updateTimer;
    private Message selectedQuestion;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    updateQuestionList();
                    if (selectedQuestion != null) {
                        updateDialogList(selectedQuestion.getDialogId());
                    }
                });
            }
        }, 0, 5000);

        questionList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                int index = questionList.getSelectionModel().getSelectedIndex();
                List<Message> questions = messageService.getUserQuestions(currentUser);
                List<Message> rootQuestions = questions.stream()
                        .filter(q -> q.getParentMessage() == null)
                        .toList();
                if (index >= 0 && index < rootQuestions.size()) {
                    selectedQuestion = rootQuestions.get(index);
                    System.out.println("Выбран вопрос: " + selectedQuestion.getTitle() + ", dialogId: " + selectedQuestion.getDialogId());
                    updateDialogList(selectedQuestion.getDialogId());
                    replyArea.setDisable(false);
                    sendReplyButton.setDisable(false);
                }
            } else {
                selectedQuestion = null;
                dialogList.getItems().clear();
                replyArea.setDisable(true);
                sendReplyButton.setDisable(true);
            }
        });

        replyArea.setDisable(true);
        sendReplyButton.setDisable(true);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateQuestionList();
    }

    @FXML
    public void createQuestion() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Заголовок и текст вопроса не могут быть пустыми.");
            return;
        }

        if (currentUser == null) {
            showAlert("Пожалуйста, войдите в систему.");
            return;
        }

        try {
            Message savedQuestion = messageService.createQuestion(currentUser, title, content);
            titleField.clear();
            contentArea.clear();
            updateQuestionList();
            showAlert("Вопрос успешно создан!");
        } catch (Exception e) {
            showAlert("Ошибка при создании вопроса: " + e.getMessage());
        }
    }

    @FXML
    public void sendReply() {
        if (selectedQuestion == null) {
            showAlert("Пожалуйста, выберите вопрос для ответа.");
            return;
        }

        String replyContent = replyArea.getText().trim();
        if (replyContent.isEmpty()) {
            showAlert("Текст ответа не может быть пустым.");
            return;
        }

        try {
            messageService.sendReply(selectedQuestion, currentUser, replyContent);
            replyArea.clear();
            updateDialogList(selectedQuestion.getDialogId());
            showAlert("Ответ успешно отправлен!");
        } catch (Exception e) {
            showAlert("Ошибка при отправке ответа: " + e.getMessage());
        }
    }

    @FXML
    public void refreshDialog() {
        if (selectedQuestion == null) {
            showAlert("Пожалуйста, выберите вопрос для обновления диалога.");
            return;
        }
        updateDialogList(selectedQuestion.getDialogId());
    }

    private void updateQuestionList() {
        if (currentUser == null) {
            questionList.getItems().clear();
            questionList.getItems().add("Пожалуйста, войдите в систему.");
            return;
        }

        List<Message> questions = messageService.getUserQuestions(currentUser);
        questionList.getItems().clear();
        List<Message> rootQuestions = questions.stream()
                .filter(q -> q.getParentMessage() == null)
                .toList();
        for (Message question : rootQuestions) {
            String receiver = question.getReceiver() != null ? question.getReceiver().getEmail() : "Не назначен";
            questionList.getItems().add(String.format("%s [%s] - Админ: %s - %s",
                    question.getTitle(), formatter.format(question.getTimestamp()), receiver,
                    question.isResolved() ? "Решено" : "В процессе"));
        }
    }

    private void updateDialogList(Long dialogId) {
        dialogList.getItems().clear();
        if (dialogId == null) {
            dialogList.getItems().add("Диалог не выбран или dialogId отсутствует.");
            System.out.println("dialogId отсутствует.");
            return;
        }

        List<Message> dialogMessages = messageService.getUserQuestions(currentUser).stream()
                .filter(m -> m.getDialogId() != null && m.getDialogId().equals(dialogId))
                .sorted((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                .toList();
        if (dialogMessages.isEmpty()) {
            dialogList.getItems().add("Сообщений в диалоге пока нет.");
            System.out.println("Сообщений для dialogId " + dialogId + " не найдено.");
            return;
        }

        for (Message message : dialogMessages) {
            try {
                String senderName = message.getSenderName();
                String content = message.getMessageDetails().getSenderMessage();
                String timestamp = formatter.format(message.getTimestamp());
                String displayMessage = String.format("%s [%s]: %s", senderName, timestamp, content);
                dialogList.getItems().add(displayMessage);
                System.out.println("Добавлено сообщение в диалог: " + displayMessage);
            } catch (Exception e) {
                dialogList.getItems().add("Ошибка парсинга: " + e.getMessage());
                System.out.println("Ошибка парсинга сообщения: " + e.getMessage());
            }
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

    public void shutdown() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }
}