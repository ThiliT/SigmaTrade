package com.example.trading.common.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PriceDto {
    String symbol;
    BigDecimal price;
    Instant timestamp;
}
