package com.example.trading.order.controller;

import com.example.trading.common.dto.OrderDto;
import com.example.trading.common.enums.OrderStatus;
import com.example.trading.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public OrderDto place(@RequestBody OrderDto dto) {
        // default to NEW for traceability
        OrderDto normalized = dto.toBuilder().status(OrderStatus.NEW).build();
        return service.placeOrder(normalized);
    }

    @GetMapping("/{id}")
    public OrderDto get(@PathVariable Long id) {
        return service.getOrder(id);
    }
}
