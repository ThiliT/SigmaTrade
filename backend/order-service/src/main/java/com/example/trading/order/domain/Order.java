package com.example.trading.order.domain;

import com.example.trading.common.enums.OrderSide;
import com.example.trading.common.enums.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "orders",
        indexes = {@Index(name = "idx_order_portfolio", columnList = "portfolioId")})
@Getter
@Setter
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long portfolioId;
    private String symbol;

    @Enumerated(EnumType.STRING)
    private OrderSide side;

    private BigDecimal quantity;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
