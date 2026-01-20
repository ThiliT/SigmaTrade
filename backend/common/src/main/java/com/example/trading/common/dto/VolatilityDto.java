package com.example.trading.common.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class VolatilityDto {
    String symbol;
    double volatility;
}
