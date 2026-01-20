package com.example.trading.order.client;

import com.example.trading.common.dto.PriceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MarketDataClient {

    private final WebClient webClient;

    public MarketDataClient(@Value("${marketdata.url:http://localhost:8081}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public PriceDto getLatestPrice(String symbol) {
        return webClient.get().uri("/prices/{symbol}", symbol).retrieve().bodyToMono(PriceDto.class).block();
    }
}
