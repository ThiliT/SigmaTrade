package com.example.trading.portfolio.controller;

import com.example.trading.common.dto.HoldingDto;
import com.example.trading.common.dto.PortfolioDto;
import com.example.trading.portfolio.service.PortfolioService;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolios")
public class PortfolioController {

    private final PortfolioService service;

    public PortfolioController(PortfolioService service) {
        this.service = service;
    }

    @PostMapping
    public PortfolioDto create(@RequestBody CreatePortfolioRequest request) {
        return service.createPortfolio(request.name(), request.cashBalance());
    }

    @PostMapping("/{id}/holdings")
    public HoldingDto addHolding(@PathVariable Long id, @RequestBody HoldingRequest request) {
        return service.addOrUpdateHolding(id, request.symbol(), request.quantity(), request.price());
    }

    @GetMapping("/{id}")
    public PortfolioDto get(@PathVariable Long id) {
        return service.getPortfolio(id);
    }

    @GetMapping("/{id}/value")
    public BigDecimal value(@PathVariable Long id) {
        return service.portfolioValue(id);
    }

    @GetMapping("/{id}/pnl")
    public BigDecimal pnl(@PathVariable Long id) {
        return service.unrealizedPnl(id);
    }

    @PostMapping("/{id}/cash")
    public PortfolioDto adjustCash(@PathVariable Long id, @RequestBody CashRequest request) {
        return service.adjustCash(id, request.delta());
    }

    public record CreatePortfolioRequest(String name, BigDecimal cashBalance) {}

    public record HoldingRequest(String symbol, BigDecimal quantity, BigDecimal price) {}

    public record CashRequest(BigDecimal delta) {}
}
