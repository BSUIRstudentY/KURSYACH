package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Commission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commissionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type", nullable = false, length = 50)
    private CommissionType commissionType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 4)
    private String currency;

    @Column(nullable = false)
    private LocalDateTime commissionDate;

    @Column(nullable = true, precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}