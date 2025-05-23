package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Asset")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Integer Id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType assetType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, length = 4)
    private String currency;

    public enum AssetType {
        АКЦИИ, ОБЛИГАЦИИ, ETF, ТРЕЙДИНГ, ФЬЮЧЕРСЫ
    }
}