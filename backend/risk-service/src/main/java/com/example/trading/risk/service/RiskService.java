package com.example.trading.risk.service;

import com.example.trading.common.dto.ExposureDto;
import com.example.trading.common.dto.HoldingDto;
import com.example.trading.common.dto.PortfolioDto;
import com.example.trading.common.dto.PriceDto;
import com.example.trading.common.dto.VarRequestDto;
import com.example.trading.common.dto.VarResponseDto;
import com.example.trading.common.dto.VolatilityDto;
import com.example.trading.risk.client.MarketDataClient;
import com.example.trading.risk.client.PortfolioClient;
import com.example.trading.risk.client.RiskEngineClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class RiskService {

    private final PortfolioClient portfolioClient;
    private final MarketDataClient marketDataClient;
    private final RiskEngineClient riskEngineClient;

    public RiskService(
            PortfolioClient portfolioClient,
            MarketDataClient marketDataClient,
            RiskEngineClient riskEngineClient) {
        this.portfolioClient = portfolioClient;
        this.marketDataClient = marketDataClient;
        this.riskEngineClient = riskEngineClient;
    }

    public VarResponseDto calculateVar(Long portfolioId) {
        PortfolioDto portfolio = portfolioClient.getPortfolio(portfolioId);
        if (portfolio == null || portfolio.getHoldings() == null) {
            return VarResponseDto.builder().var95(0).build();
        }
        List<Double> pnlSeries = new ArrayList<>();

        for (HoldingDto holding : portfolio.getHoldings()) {
            List<PriceDto> history = marketDataClient.history(holding.getSymbol(), 30);
            if (history == null || history.size() < 2) {
                continue;
            }
            for (int i = 1; i < history.size(); i++) {
                BigDecimal prev = history.get(i - 1).getPrice();
                BigDecimal curr = history.get(i).getPrice();
                double pnl = curr.subtract(prev).doubleValue() * holding.getQuantity().doubleValue();
                if (pnlSeries.size() < i) {
                    pnlSeries.add(pnl);
                } else {
                    pnlSeries.set(i - 1, pnlSeries.get(i - 1) + pnl);
                }
            }
        }

        if (pnlSeries.isEmpty()) {
            return VarResponseDto.builder().var95(0).build();
        }

        VarRequestDto requestDto = VarRequestDto.builder().pnl(pnlSeries).build();
        return riskEngineClient.computeVar(requestDto);
    }

    public List<ExposureDto> exposure(Long portfolioId) {
        PortfolioDto portfolio = portfolioClient.getPortfolio(portfolioId);
        if (portfolio == null || portfolio.getHoldings() == null) {
            return List.of();
        }
        return portfolio.getHoldings().stream()
                .map(
                        h -> {
                            PriceDto price = marketDataClient.latest(h.getSymbol());
                            BigDecimal exposure =
                                    (price == null ? BigDecimal.ZERO : price.getPrice())
                                            .multiply(h.getQuantity())
                                            .setScale(2, RoundingMode.HALF_UP);
                            return ExposureDto.builder()
                                    .symbol(h.getSymbol())
                                    .exposure(exposure)
                                    .build();
                        })
                .toList();
    }

    public VolatilityDto volatility(String symbol) {
        List<PriceDto> history = marketDataClient.history(symbol, 30);
        if (history == null || history.size() < 2) {
            return VolatilityDto.builder().symbol(symbol).volatility(0).build();
        }
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < history.size(); i++) {
            double prev = history.get(i - 1).getPrice().doubleValue();
            double curr = history.get(i).getPrice().doubleValue();
            returns.add((curr - prev) / prev);
        }
        double mean =
                returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance =
                returns.stream()
                        .mapToDouble(r -> Math.pow(r - mean, 2))
                        .sum()
                        / (returns.size() - 1);
        double stdDev = Math.sqrt(variance);
        return VolatilityDto.builder().symbol(symbol).volatility(stdDev).build();
    }
}
