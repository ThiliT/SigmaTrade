package com.example.trading.portfolio.mapper;

import com.example.trading.common.dto.HoldingDto;
import com.example.trading.common.dto.PortfolioDto;
import com.example.trading.portfolio.domain.Holding;
import com.example.trading.portfolio.domain.Portfolio;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PortfolioMapper {
    @Mapping(target = "holdings", ignore = true)
    PortfolioDto toDto(Portfolio portfolio);

    HoldingDto toHoldingDto(Holding holding);

    default List<HoldingDto> toHoldingDtos(List<Holding> holdings) {
        return holdings.stream().map(this::toHoldingDto).toList();
    }
}
