package com.example.trading.common.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class VarRequestDto {
    List<Double> pnl;
}
