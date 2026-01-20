package com.example.trading.marketdata.service;

import com.example.trading.common.dto.PriceDto;
import com.example.trading.marketdata.domain.AssetPrice;
import com.example.trading.marketdata.mapper.AssetPriceMapper;
import com.example.trading.marketdata.repository.AssetPriceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class PriceSimulationService {

    private static final List<String> SYMBOLS = List.of("AAPL", "TSLA", "AMZN", "BTC", "ETH");

    private final Map<String, BigDecimal> lastPrices;
    private final Random random = new Random();
    private final AssetPriceRepository repository;
    private final AssetPriceMapper mapper;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public PriceSimulationService(AssetPriceRepository repository, AssetPriceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.lastPrices =
                SYMBOLS.stream()
                        .collect(
                                java.util.stream.Collectors.toMap(
                                        symbol -> symbol, this::initialPrice));
    }

    private BigDecimal initialPrice(String symbol) {
        return switch (symbol) {
            case "AAPL" -> BigDecimal.valueOf(180);
            case "TSLA" -> BigDecimal.valueOf(200);
            case "AMZN" -> BigDecimal.valueOf(140);
            case "BTC" -> BigDecimal.valueOf(40000);
            case "ETH" -> BigDecimal.valueOf(2200);
            default -> BigDecimal.valueOf(100);
        };
    }

    @Scheduled(fixedDelay = 5000)
    public void generatePrices() {
        Instant now = Instant.now();
        for (String symbol : SYMBOLS) {
            BigDecimal previous = lastPrices.get(symbol);
            BigDecimal newPrice = driftPrice(previous);
            lastPrices.put(symbol, newPrice);
            AssetPrice saved = repository.save(new AssetPrice(symbol, newPrice, now));
            sendToSubscribers(mapper.toDto(saved));
        }
    }

    private BigDecimal driftPrice(BigDecimal price) {
        double change = random.nextGaussian() * 0.01; // ~1% std dev
        BigDecimal factor = BigDecimal.valueOf(1 + change);
        return price.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public PriceDto latestPrice(String symbol) {
        return repository
                .findTopBySymbolOrderByTimestampDesc(symbol)
                .map(mapper::toDto)
                .orElse(null);
    }

    public List<PriceDto> history(String symbol, Instant after) {
        return repository.findBySymbolAndTimestampAfterOrderByTimestampAsc(symbol, after).stream()
                .map(mapper::toDto)
                .toList();
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    private void sendToSubscribers(PriceDto dto) {
        emitters.forEach(
                emitter -> {
                    try {
                        emitter.send(dto);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                        emitters.remove(emitter);
                    }
                });
    }
}
