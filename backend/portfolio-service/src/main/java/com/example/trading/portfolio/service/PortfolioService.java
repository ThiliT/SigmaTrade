package com.example.trading.portfolio.service;

import com.example.trading.common.dto.HoldingDto;
import com.example.trading.common.dto.PortfolioDto;
import com.example.trading.common.dto.PriceDto;
import com.example.trading.portfolio.client.MarketDataClient;
import com.example.trading.portfolio.domain.Holding;
import com.example.trading.portfolio.domain.Portfolio;
import com.example.trading.portfolio.mapper.PortfolioMapper;
import com.example.trading.portfolio.repository.HoldingRepository;
import com.example.trading.portfolio.repository.PortfolioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final PortfolioMapper mapper;
    private final MarketDataClient marketDataClient;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            HoldingRepository holdingRepository,
            PortfolioMapper mapper,
            MarketDataClient marketDataClient) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.mapper = mapper;
        this.marketDataClient = marketDataClient;
    }

    @Transactional
    public PortfolioDto createPortfolio(String name, BigDecimal cashBalance) {
        Portfolio portfolio = new Portfolio(name, cashBalance);
        Portfolio saved = portfolioRepository.save(portfolio);
        PortfolioDto dto = mapper.toDto(saved);
        return dto.toBuilder().holdings(List.of()).build();
    }

    @Transactional
    public HoldingDto addOrUpdateHolding(Long portfolioId, String symbol, BigDecimal quantity, BigDecimal price) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow();
        Holding holding =
                holdingRepository
                        .findByPortfolioIdAndSymbol(portfolioId, symbol)
                        .orElseGet(
                                () -> {
                                    Holding h = new Holding();
                                    h.setPortfolio(portfolio);
                                    h.setSymbol(symbol);
                                    h.setQuantity(BigDecimal.ZERO);
                                    h.setAvgBuyPrice(BigDecimal.ZERO);
                                    return h;
                                });

        BigDecimal existingQty = holding.getQuantity();
        BigDecimal newQty = existingQty.add(quantity);

        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cannot reduce holding below zero");
        }

        BigDecimal newAvgPrice =
                newQty.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : existingQty.multiply(holding.getAvgBuyPrice())
                                .add(quantity.multiply(price))
                                .divide(newQty, 4, RoundingMode.HALF_UP);

        holding.setQuantity(newQty);
        holding.setAvgBuyPrice(newAvgPrice);
        Holding saved = holdingRepository.save(holding);
        return mapper.toHoldingDto(saved);
    }

    @Transactional(readOnly = true)
    public PortfolioDto getPortfolio(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow();
        PortfolioDto dto = mapper.toDto(portfolio);
        return dto.toBuilder().holdings(mapper.toHoldingDtos(portfolio.getHoldings())).build();
    }

    @Transactional(readOnly = true)
    public BigDecimal portfolioValue(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow();
        BigDecimal holdingsValue =
                portfolio.getHoldings().stream()
                        .map(
                                h -> {
                                    PriceDto price = marketDataClient.getLatestPrice(h.getSymbol());
                                    BigDecimal px =
                                            price == null ? BigDecimal.ZERO : price.getPrice();
                                    return px.multiply(h.getQuantity());
                                })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        return portfolio.getCashBalance().add(holdingsValue);
    }

    @Transactional(readOnly = true)
    public BigDecimal unrealizedPnl(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow();
        return portfolio.getHoldings().stream()
                .map(
                        h -> {
                            PriceDto price = marketDataClient.getLatestPrice(h.getSymbol());
                            BigDecimal current = price == null ? BigDecimal.ZERO : price.getPrice();
                            BigDecimal cost = h.getAvgBuyPrice().multiply(h.getQuantity());
                            BigDecimal marketValue = current.multiply(h.getQuantity());
                            return marketValue.subtract(cost);
                        })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public PortfolioDto adjustCash(Long id, BigDecimal delta) {
        Portfolio portfolio = portfolioRepository.findById(id).orElseThrow();
        portfolio.setCashBalance(portfolio.getCashBalance().add(delta));
        Portfolio saved = portfolioRepository.save(portfolio);
        return mapper.toDto(saved).toBuilder().holdings(mapper.toHoldingDtos(saved.getHoldings())).build();
    }
}
