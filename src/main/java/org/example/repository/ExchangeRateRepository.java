package org.example.repository;

import org.example.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Integer> {
    Optional<ExchangeRate> findByFromCurrencyAndToCurrency(ExchangeRate.Currency fromCurrency, ExchangeRate.Currency toCurrency);
}