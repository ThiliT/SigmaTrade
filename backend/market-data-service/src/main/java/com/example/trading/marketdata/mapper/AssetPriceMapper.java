package com.example.trading.marketdata.mapper;

import com.example.trading.common.dto.PriceDto;
import com.example.trading.marketdata.domain.AssetPrice;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AssetPriceMapper {
    PriceDto toDto(AssetPrice entity);
}
