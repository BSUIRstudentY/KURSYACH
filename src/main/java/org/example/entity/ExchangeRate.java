package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ExchangeRate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer exchangeRateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency fromCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency toCurrency;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal rate;

    public enum Currency {
        USD, RUB, BYN, EUR
    }
}