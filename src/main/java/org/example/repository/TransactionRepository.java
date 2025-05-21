package org.example.repository;

import org.example.entity.Transaction;
import org.example.entity.User;
import org.example.entity.Transaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    // Поиск всех транзакций пользователя
    List<Transaction> findByUser(User user);

    // Поиск транзакций пользователя по типу
    List<Transaction> findByUserAndTransactionType(User user, TransactionType transactionType);
}