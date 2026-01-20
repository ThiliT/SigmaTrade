package com.example.trading.common.dto;

import com.example.trading.common.enums.OrderSide;
import com.example.trading.common.enums.OrderStatus;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class OrderDto {
    Long id;
    Long portfolioId;
    String symbol;
    OrderSide side;
    BigDecimal quantity;
    BigDecimal price;
    OrderStatus status;
}
