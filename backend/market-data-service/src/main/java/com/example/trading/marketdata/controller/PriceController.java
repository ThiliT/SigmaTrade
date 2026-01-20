package com.example.trading.marketdata.controller;

import com.example.trading.common.dto.PriceDto;
import com.example.trading.marketdata.service.PriceSimulationService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/prices")
public class PriceController {

    private final PriceSimulationService service;

    public PriceController(PriceSimulationService service) {
        this.service = service;
    }

    @GetMapping("/{symbol}")
    public PriceDto latestPrice(@PathVariable String symbol) {
        return service.latestPrice(symbol);
    }

    @GetMapping("/{symbol}/history")
    public List<PriceDto> history(
            @PathVariable String symbol, @RequestParam(defaultValue = "30") int days) {
        Instant after = Instant.now().minus(days, ChronoUnit.DAYS);
        return service.history(symbol, after);
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return service.createEmitter();
    }
}
