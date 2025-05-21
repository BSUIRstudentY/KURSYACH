package org.example.service;

import org.example.entity.Message;
import org.example.entity.User;
import org.example.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Transactional
    public Message createQuestion(User sender, String title, String content) {
        Message message = Message.builder()
                .sender(sender)
                .receiver(null)
                .title(title)
                .content(content)
                .timestamp(LocalDateTime.now())
                .isResolved(false)
                .parentMessage(null)
                .build();
        Message savedMessage = messageRepository.save(message);
        if (savedMessage.getDialogId() == null) {
            savedMessage.setDialogId(savedMessage.getId());
            messageRepository.save(savedMessage);
        }
        return savedMessage;
    }

    public List<Message> getUserQuestions(User sender) {
        return messageRepository.findBySender(sender);
    }

    @Transactional
    public Message sendReply(Message parentMessage, User sender, String replyContent) {
        Message reply = Message.builder()
                .sender(sender)
                .receiver(parentMessage.getSender()) // Админ отвечает пользователю
                .title("Ответ на: " + parentMessage.getTitle())
                .content(replyContent)
                .timestamp(LocalDateTime.now())
                .isResolved(false)
                .parentMessage(parentMessage)
                .dialogId(parentMessage.getDialogId())
                .build();
        return messageRepository.save(reply);
    }
}