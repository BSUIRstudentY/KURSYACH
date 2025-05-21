package org.example.repository;

import org.example.entity.Message;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByIsResolvedFalse();

    List<Message> findByDialogIdOrderByTimestampAsc(Long dialogId);

    List<Message> findBySender(User sender);

    List<Message> findByIsResolvedFalseAndParentMessageIsNull();

}