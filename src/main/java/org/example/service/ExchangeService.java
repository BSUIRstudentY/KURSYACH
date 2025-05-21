package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Account;
import org.example.entity.ExchangeRate;
import org.example.entity.Transaction;
import org.example.repository.AccountRepository;
import org.example.repository.ExchangeRateRepository;
import org.example.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExchangeService {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public ExchangeRate getExchangeRate(String fromCurrency, String toCurrency) {
        return exchangeRateRepository.findByFromCurrencyAndToCurrency(
                ExchangeRate.Currency.valueOf(fromCurrency),
                ExchangeRate.Currency.valueOf(toCurrency)
        ).orElseThrow(() -> new IllegalArgumentException("Курс обмена не найден для " + fromCurrency + " -> " + toCurrency));
    }

    @Transactional
    public void exchangeCurrency(Account fromAccount, Account toAccount, String fromCurrency, String toCurrency, BigDecimal amount, BigDecimal convertedAmount) throws Exception {
        // Парсинг балансов исходного счета
        Map<String, Double> fromBalances = parseBalances(fromAccount.getBalances());
        double fromBalance = fromBalances.getOrDefault(fromCurrency, 0.0);
        if (BigDecimal.valueOf(fromBalance).compareTo(amount) < 0) {
            throw new IllegalStateException("Недостаточно средств на счете в валюте " + fromCurrency + ".");
        }
        fromBalance -= amount.doubleValue();
        fromBalances.put(fromCurrency, fromBalance);
        fromAccount.setBalances(objectMapper.writeValueAsString(fromBalances));

        // Парсинг балансов целевого счета
        Map<String, Double> toBalances = parseBalances(toAccount.getBalances());
        double toBalance = toBalances.getOrDefault(toCurrency, 0.0);
        toBalance += convertedAmount.doubleValue();
        toBalances.put(toCurrency, toBalance);
        toAccount.setBalances(objectMapper.writeValueAsString(toBalances));

        // Создание одной транзакции типа "ОБМЕН"
        Transaction exchangeTransaction = Transaction.builder()
                .user(fromAccount.getUser())
                .transactionType(Transaction.TransactionType.ОБМЕН)
                .amount(amount)
                .currency(fromCurrency)
                .toCurrency(toCurrency)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .transactionDate(LocalDateTime.now())
                .build();

        // Сохранение транзакции и счетов
        transactionRepository.save(exchangeTransaction);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    private Map<String, Double> parseBalances(String balancesJson) throws Exception {
        if (balancesJson == null || balancesJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        @SuppressWarnings("unchecked")
        Map<String, Double> balances = objectMapper.readValue(balancesJson, Map.class);
        return balances;
    }
}