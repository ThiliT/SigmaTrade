package com.example.trading.risk.client;

import com.example.trading.common.dto.VarRequestDto;
import com.example.trading.common.dto.VarResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class RiskEngineClient {

    private final WebClient webClient;

    public RiskEngineClient(@Value("${risk.engine.url:http://localhost:8090}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public VarResponseDto computeVar(VarRequestDto requestDto) {
        return webClient
                .post()
                .uri("/compute/var")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(VarResponseDto.class)
                .block();
    }
}
