package com.example.trading.risk.client;

import com.example.trading.common.dto.PriceDto;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MarketDataClient {

    private final WebClient webClient;

    public MarketDataClient(@Value("${marketdata.url:http://localhost:8081}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public List<PriceDto> history(String symbol, int days) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/prices/{symbol}/history").queryParam("days", days).build(symbol))
                .retrieve()
                .bodyToFlux(PriceDto.class)
                .collectList()
                .block();
    }

    public PriceDto latest(String symbol) {
        return webClient.get().uri("/prices/{symbol}", symbol).retrieve().bodyToMono(PriceDto.class).block();
    }
}
