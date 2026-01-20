package com.example.trading.common.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class PortfolioDto {
    Long id;
    String name;
    BigDecimal cashBalance;
    List<HoldingDto> holdings;
}
