package com.example.trading.common.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ExposureDto {
    String symbol;
    BigDecimal exposure;
}
