package br.allandemiranda.fx.controller.impl;

import br.allandemiranda.fx.dto.EMAsDto;
import br.allandemiranda.fx.dto.GarchDto;
import br.allandemiranda.fx.dto.GarchTradingDto;
import br.allandemiranda.fx.dto.MACDDto;
import br.allandemiranda.fx.dto.RSIDto;
import br.allandemiranda.fx.service.CandlestickService;
import br.allandemiranda.fx.service.EmasService;
import br.allandemiranda.fx.service.GarchService;
import br.allandemiranda.fx.service.GarchTradingService;
import br.allandemiranda.fx.service.MacdService;
import br.allandemiranda.fx.service.RsiService;
import br.allandemiranda.fx.service.ValidateBean;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/ml")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MLControllerImpl {

    private final CandlestickService candlestickService;
    private final EmasService emasService;
    private final GarchService garchService;
    private final GarchTradingService garchTradingService;
    private final MacdService macdService;
    private final RsiService rsiService;
    private final Executor executor;
    private final ValidateBean validateBean;

    @PostMapping
    public ResponseEntity<Void> generateML(@RequestParam @Valid @NotNull Path candlesticksFile, @RequestParam @NotNull @Valid Path saveLocation) {
        // record Data(@PastOrPresent LocalDateTime timestamp, EMAsDto emasDto0, EMAsDto emasDto1, EMAsDto emasDto2, GarchDto garchDto0, GarchDto garchDto1, GarchDto garchDto2, MACDDto macdDto0, MACDDto macdDto1, MACDDto macdDto2, RSIDto rsiDto0, RSIDto rsiDto1, RSIDto rsiDto2, GarchTradingDto tradingDto) {}

        record Data(@PastOrPresent LocalDateTime timestamp, EMAsDto emas, MACDDto macd, RSIDto rsi, GarchDto garch, GarchTradingDto trading) {
        }
        LinkedList<Data> lastThree = new LinkedList<>();
        this.getCandlestickService().consumerCandlesticks(candlesticksFile, candlestick -> {

        });

        return ResponseEntity.ok().build();
    }
}
