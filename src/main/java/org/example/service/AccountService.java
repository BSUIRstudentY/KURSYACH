package org.example.service;

import org.example.entity.Account;
import org.example.entity.User;
import org.example.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

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

            accountRepository.save(account);
            System.out.println("Account created with ID: " + account.getAccountId());
        } catch (Exception e) {
            System.err.println("Failed to create account: " + e.getMessage());
            throw new RuntimeException("Failed to create account: " + e.getMessage(), e);
        }
    }

    public List<Account> getAccountsByUser(User user) {
        try {
            List<Account> accounts = accountRepository.findByUser(user);
            System.out.println("Fetched accounts for user " + user.getUserId() + ": " + accounts.size());
            return accounts;
        } catch (Exception e) {
            System.err.println("Failed to fetch accounts for user " + user.getUserId() + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch accounts: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteAccount(Account account) {
        System.out.println("DeleteAccount method called for account ID: " + account.getAccountId());
        try {
            System.out.println("Attempting to remove account with ID: " + account.getAccountId() + " from user's accounts list.");
            // Находим управляемую сущность
            Account managedAccount = entityManager.find(Account.class, account.getAccountId());
            if (managedAccount != null) {
                User user = managedAccount.getUser();
                if (user != null) {
                    // Удаляем счет из списка accounts у пользователя
                    user.getAccounts().remove(managedAccount);
                    System.out.println("Account with ID " + account.getAccountId() + " removed from user's accounts list.");
                }
                // Разрываем связь, устанавливая user в null
                managedAccount.setUser(null);
                System.out.println("Account with ID " + account.getAccountId() + " user set to null in database.");
                // Сохраняем изменения
                entityManager.merge(managedAccount);
                // Принудительно записываем изменения в базу данных
                entityManager.flush();
                System.out.println("Changes flushed to database for account ID: " + account.getAccountId());
            } else {
                System.out.println("Account with ID " + account.getAccountId() + " not found.");
            }
            // Очищаем кэш Hibernate
            entityManager.clear();
            System.out.println("EntityManager cache cleared.");
        } catch (Exception e) {
            System.err.println("Failed to remove account with ID " + account.getAccountId() + " from user's accounts: " + e.getMessage());
            throw new RuntimeException("Failed to remove account from user's accounts: " + e.getMessage(), e);
        }
    }
}