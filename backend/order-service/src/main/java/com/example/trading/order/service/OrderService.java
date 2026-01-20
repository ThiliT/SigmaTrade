package com.example.trading.order.service;

import com.example.trading.common.dto.OrderDto;
import com.example.trading.common.dto.PortfolioDto;
import com.example.trading.common.dto.PriceDto;
import com.example.trading.common.enums.OrderSide;
import com.example.trading.common.enums.OrderStatus;
import com.example.trading.order.client.MarketDataClient;
import com.example.trading.order.client.PortfolioClient;
import com.example.trading.order.domain.Order;
import com.example.trading.order.mapper.OrderMapper;
import com.example.trading.order.repository.OrderRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final MarketDataClient marketClient;
    private final PortfolioClient portfolioClient;

    public OrderService(
            OrderRepository repository,
            OrderMapper mapper,
            MarketDataClient marketClient,
            PortfolioClient portfolioClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.marketClient = marketClient;
        this.portfolioClient = portfolioClient;
    }

    @Transactional
    public OrderDto placeOrder(OrderDto request) {
        PriceDto priceDto = marketClient.getLatestPrice(request.getSymbol());
        if (priceDto == null) {
            return reject(request, "Price not available");
        }
        BigDecimal executionPrice = priceDto.getPrice();
        PortfolioDto portfolio = portfolioClient.getPortfolio(request.getPortfolioId());
        if (portfolio == null) {
            return reject(request, "Portfolio not found");
        }

        BigDecimal notional = executionPrice.multiply(request.getQuantity());

        if (request.getSide() == OrderSide.BUY) {
            if (portfolio.getCashBalance().compareTo(notional) < 0) {
                return reject(request, "Insufficient cash");
            }
            portfolioClient.adjustCash(request.getPortfolioId(), notional.negate().doubleValue());
            portfolioClient.updateHolding(
                    request.getPortfolioId(),
                    request.getSymbol(),
                    request.getQuantity().doubleValue(),
                    executionPrice.doubleValue());
        } else {
            double heldQty =
                    portfolio.getHoldings().stream()
                            .filter(h -> h.getSymbol().equalsIgnoreCase(request.getSymbol()))
                            .findFirst()
                            .map(h -> h.getQuantity().doubleValue())
                            .orElse(0d);
            if (BigDecimal.valueOf(heldQty).compareTo(request.getQuantity()) < 0) {
                return reject(request, "Insufficient quantity");
            }
            portfolioClient.updateHolding(
                    request.getPortfolioId(),
                    request.getSymbol(),
                    request.getQuantity().negate().doubleValue(),
                    executionPrice.doubleValue());
            portfolioClient.adjustCash(request.getPortfolioId(), notional.doubleValue());
        }

        Order entity = mapper.toEntity(request);
        entity.setStatus(OrderStatus.FILLED);
        entity.setPrice(executionPrice);
        Order saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    public OrderDto getOrder(Long id) {
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    private OrderDto reject(OrderDto request, String reason) {
        Order entity = mapper.toEntity(request);
        entity.setStatus(OrderStatus.REJECTED);
        entity.setPrice(request.getPrice());
        Order saved = repository.save(entity);
        return mapper.toDto(saved);
    }
}
