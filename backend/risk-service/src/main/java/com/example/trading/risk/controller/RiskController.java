package com.example.trading.risk.controller;

import com.example.trading.common.dto.ExposureDto;
import com.example.trading.common.dto.VarResponseDto;
import com.example.trading.common.dto.VolatilityDto;
import com.example.trading.risk.service.RiskService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/risk")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/var/{portfolioId}")
    public VarResponseDto var(@PathVariable Long portfolioId) {
        return riskService.calculateVar(portfolioId);
    }

    @GetMapping("/exposure/{portfolioId}")
    public List<ExposureDto> exposure(@PathVariable Long portfolioId) {
        return riskService.exposure(portfolioId);
    }

    @GetMapping("/volatility/{symbol}")
    public VolatilityDto volatility(@PathVariable String symbol) {
        return riskService.volatility(symbol);
    }
}
