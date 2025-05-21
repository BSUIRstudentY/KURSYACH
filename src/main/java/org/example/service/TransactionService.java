package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Account;
import org.example.entity.Transaction;
import org.example.entity.User;
import org.example.repository.AccountRepository;
import org.example.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void createTransaction(Transaction transaction) throws JsonProcessingException {
        // Проверка, что asset не null и имеет ID для операций с активами
        if (transaction.getTransactionType() == Transaction.TransactionType.ПОКУПКА ||
                transaction.getTransactionType() == Transaction.TransactionType.ПРОДАЖА) {
            if (transaction.getAsset() == null || transaction.getAsset().getId() == null) {
                throw new IllegalStateException("Актив для транзакции не указан или не существует.");
            }
        }

        // Логирование для отладки
        System.out.println("Создание транзакции: " + transaction.getTransactionType() + ", пользователь: " +
                (transaction.getUser() != null ? transaction.getUser().getEmail() : "null") +
                ", toAccount: " + (transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : "null") +
                ", fromAccount: " + (transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : "null"));

        // Сохранение транзакции
        transactionRepository.save(transaction);

        // Обработка операций с активами (например, ПОКУПКА)
        if (transaction.getTransactionType() == Transaction.TransactionType.ПОКУПКА) {
            Account fromAccount = transaction.getFromAccount();
            if (fromAccount != null) {
                Map<String, Double> balances = parseBalances(fromAccount.getBalances());
                String currency = transaction.getCurrency();
                double currentBalance = balances.getOrDefault(currency, 0.0);
                double transactionAmount = transaction.getAmount().doubleValue();
                if (currentBalance < transactionAmount) {
                    throw new IllegalStateException("Недостаточно средств на счете в валюте " + currency + ".");
                }
                balances.put(currency, currentBalance - transactionAmount);
                fromAccount.setBalances(objectMapper.writeValueAsString(balances));
                accountRepository.save(fromAccount);
                System.out.println("Баланс счета " + fromAccount.getAccountNumber() + " обновлен: " + balances);
            } else {
                throw new IllegalStateException("Исходный счет для покупки не указан.");
            }
        }
    }

    @Transactional
    public void createTransaction(Transaction transaction, Account account, Map<String, Double> balances) throws Exception {
        // Проверка на null для account
        if (account == null) {
            throw new IllegalStateException("Счет для транзакции не указан.");
        }

        String currency = transaction.getCurrency();
        double amount = transaction.getAmount().doubleValue();

        // Обновление баланса
        double currentBalance = balances.getOrDefault(currency, 0.0);
        if (transaction.getTransactionType() == Transaction.TransactionType.ПОПОЛНЕНИЕ) {
            currentBalance += amount;
        } else if (transaction.getTransactionType() == Transaction.TransactionType.СНЯТИЕ) {
            if (currentBalance < amount) {
                throw new IllegalStateException("Недостаточно средств на счете в валюте " + currency + ".");
            }
            currentBalance -= amount;
        }
        balances.put(currency, currentBalance);

        // Сохранение обновленного баланса в JSON
        account.setBalances(objectMapper.writeValueAsString(balances));

        // Логирование для отладки
        System.out.println("Создание транзакции: " + transaction.getTransactionType() + ", пользователь: " +
                (transaction.getUser() != null ? transaction.getUser().getEmail() : "null") +
                ", счет: " + account.getAccountNumber() + ", новый баланс: " + balances);

        // Сохранение транзакции и счета
        transactionRepository.save(transaction);
        accountRepository.save(account);
    }

    private Map<String, Double> parseBalances(String balancesJson) throws JsonProcessingException {
        if (balancesJson == null || balancesJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        @SuppressWarnings("unchecked")
        Map<String, Double> balances = objectMapper.readValue(balancesJson, Map.class);
        return balances;
    }

    public List<Transaction> findByUser(User user) {
        if (user == null || user.getUserId() == null) {
            System.out.println("Пользователь null или отсутствует userId.");
            return List.of();
        }
        System.out.println("Поиск транзакций для userId: " + user.getUserId());
        return transactionRepository.findByUser(user);
    }
}