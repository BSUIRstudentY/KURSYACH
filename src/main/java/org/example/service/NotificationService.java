package org.example.service;

import org.example.entity.Notification;
import org.example.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void addNotification(Long userId, String content, NotificationType type) {
        String formattedContent = switch (type) {
            case SUPPORT_RESPONSE -> "Ответ техподдержки: " + content;
            case ADMIN_ACTION -> "Действие администратора: " + content;
            case BROADCAST -> "Рассылка: " + content;
        };
        Notification notification = Notification.builder()
                .userId(userId)
                .content(formattedContent)
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public int getUnreadNotificationsCount(Long userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false).size();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsRead(userId, false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}

enum NotificationType {
    SUPPORT_RESPONSE,
    ADMIN_ACTION,
    BROADCAST
}