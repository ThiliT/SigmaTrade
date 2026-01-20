package com.example.trading.marketdata.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "asset_price",
        indexes = {
            @Index(name = "idx_asset_price_symbol_time", columnList = "symbol,timestamp")
        })
@Getter
@Setter
@NoArgsConstructor
public class AssetPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private BigDecimal price;
    private Instant timestamp;

    public AssetPrice(String symbol, BigDecimal price, Instant timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }
}
