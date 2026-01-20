package com.example.trading.order.mapper;

import com.example.trading.common.dto.OrderDto;
import com.example.trading.order.domain.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    Order toEntity(OrderDto dto);

    OrderDto toDto(Order entity);
}
