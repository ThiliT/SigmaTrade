package com.example.trading.order.client;

import com.example.trading.common.dto.HoldingDto;
import com.example.trading.common.dto.PortfolioDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PortfolioClient {

    private final WebClient webClient;

    public PortfolioClient(@Value("${portfolio.url:http://localhost:8082}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public PortfolioDto getPortfolio(Long id) {
        return webClient.get().uri("/portfolios/{id}", id).retrieve().bodyToMono(PortfolioDto.class).block();
    }

    public HoldingDto updateHolding(Long id, String symbol, double quantity, double price) {
        record HoldingRequest(String symbol, double quantity, double price) {}
        return webClient
                .post()
                .uri("/portfolios/{id}/holdings", id)
                .bodyValue(new HoldingRequest(symbol, quantity, price))
                .retrieve()
                .bodyToMono(HoldingDto.class)
                .block();
    }

    public PortfolioDto adjustCash(Long id, double delta) {
        record CashRequest(double delta) {}
        Mono<PortfolioDto> response =
                webClient
                        .post()
                        .uri("/portfolios/{id}/cash", id)
                        .bodyValue(new CashRequest(delta))
                        .retrieve()
                        .bodyToMono(PortfolioDto.class);
        return response.block();
    }
}
