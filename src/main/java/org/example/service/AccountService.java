package org.example.service;

import org.example.entity.Account;
import org.example.entity.User;
import org.example.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public void createAccount(Account account) {
        try {
            if (account.getName() == null || account.getName().trim().isEmpty()) {
                account.setName("БАЗОВЫЙ");
            }
            if (account.getAccountType() == null) {
                account.setAccountType(Account.AccountType.ВАЛЮТНЫЙ);
            }
            account.setCreatedAt(LocalDateTime.now());
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    accountRepository.save(account);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to create account: " + e.getMessage(), e);
        }
    }

    public List<Account> getAccountsByUser(User user) {
        try {
            return accountRepository.findByUser(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch accounts for user: " + user.getUserId(), e);
        }
    }

    public void deleteAccount(Account account) {
        try {
            System.out.println("Attempting to delete account with ID: " + account.getAccountId());
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Account managedAccount = entityManager.merge(account);
                    accountRepository.deleteById(managedAccount.getAccountId());
                    entityManager.flush();
                    entityManager.clear();
                    System.out.println("Account with ID " + managedAccount.getAccountId() + " deleted successfully.");
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to delete account with ID " + account.getAccountId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete account: " + e.getMessage(), e);
        }
    }
}