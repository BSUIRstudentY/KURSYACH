package org.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonProperty("sender")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    @JsonProperty("receiver")
    private User receiver;

    @Column(nullable = false)
    @JsonProperty("title")
    private String title;

    @Column(nullable = false)
    @JsonProperty("content")
    private String content;

    @Column(nullable = false)
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Column(name = "is_resolved", nullable = false)
    @Builder.Default
    @JsonProperty("is_resolved")
    private boolean isResolved = false;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonProperty("parent_message")
    private Message parentMessage;

    @Column(name = "dialog_id")
    @JsonProperty("dialog_id")
    private Long dialogId;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (dialogId == null && parentMessage == null) {
            // Устанавливаем dialogId равным id после сохранения
            dialogId = id; // Это будет перезаписано в @PostPersist, если id сгенерирован
        } else if (parentMessage != null && dialogId == null) {
            dialogId = parentMessage.getDialogId();
        }
    }

    @PostPersist
    protected void onPostPersist() {
        if (dialogId == null && parentMessage == null) {
            dialogId = id; // Гарантируем, что dialogId установлен после генерации id

        }
    }

    @JsonProperty("sender_name")
    public String getSenderName() {
        return sender != null ? sender.getFirstName() + " " + sender.getLastName() : "Неизвестный";
    }

    @JsonProperty("message_details")
    public MessageDetails getMessageDetails() {
        return new MessageDetails(this);
    }

    @Data
    public static class MessageDetails {
        @JsonProperty("sender_message")
        private final String senderMessage;
        @JsonProperty("receiver_message")
        private final String receiverMessage;

        public MessageDetails(Message message) {
            this.senderMessage = message.getSender() != null ? message.getContent() : null;
            this.receiverMessage = message.getReceiver() != null ? message.getContent() : null;
        }
    }
}