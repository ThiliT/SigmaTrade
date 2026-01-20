package com.example.trading.risk.client;

import com.example.trading.common.dto.PortfolioDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PortfolioClient {

    private final WebClient webClient;

    public PortfolioClient(@Value("${portfolio.url:http://localhost:8082}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public PortfolioDto getPortfolio(Long id) {
        return webClient.get().uri("/portfolios/{id}", id).retrieve().bodyToMono(PortfolioDto.class).block();
    }
}
