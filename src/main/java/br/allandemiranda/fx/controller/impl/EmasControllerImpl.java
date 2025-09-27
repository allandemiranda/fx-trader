package br.allandemiranda.fx.controller.impl;

import br.allandemiranda.fx.controller.EmasController;
import br.allandemiranda.fx.dto.EMAsDto;
import br.allandemiranda.fx.enums.AppliedPrice;
import br.allandemiranda.fx.service.CandlestickService;
import br.allandemiranda.fx.service.EmasService;
import br.allandemiranda.fx.utils.MathUtils;
import br.allandemiranda.fx.utils.MovingAverageUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/emas")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class EmasControllerImpl implements EmasController {

    private final CandlestickService candlestickService;
    private final EmasService emasService;
    private final Executor executor;

    private static void calculateEma(int emaPeriod, @NotNull LinkedList<BigDecimal> emaPrices, BigDecimal price, BigDecimal[] emas, int index) {
        if (emaPrices.size() < emaPeriod) {
            emaPrices.addLast(price);
            if (emaPrices.size() == emaPeriod) {
                emas[index] = MovingAverageUtils.getSimpleMovingAverage(emaPrices, emaPeriod);
                emaPrices.clear();
            }
        } else {
            emas[index] = MovingAverageUtils.getExponentialMovingAverage(emas[index], price, emaPeriod);
        }
    }

    @PostMapping
    @Override
    public ResponseEntity<Void> generateAllEmas(@RequestParam @Valid @NotNull Path candlestickFile, @RequestParam @Min(2) int emaHighPeriod, @RequestParam @Min(2) int emaMidPeriod, @RequestParam @Min(2) int emaLowPeriod, @RequestParam @Valid @NotNull AppliedPrice appliedTo) {
        this.getExecutor().execute(this.getRunnableJob(candlestickFile, emaHighPeriod, emaMidPeriod, emaLowPeriod, appliedTo));
        return ResponseEntity.ok().build();
    }

    protected @NotNull Runnable getRunnableJob(@NotNull Path candlestickFile, int emaHighPeriod, int emaMidPeriod, int emaLowPeriod, @NotNull AppliedPrice appliedTo) {
        return () -> {
            LocalDateTime start = LocalDateTime.now();
            log.info("Generate all EMAs(emaHighPeriod={},emaMidPeriod={},emaLowPeriod={},appliedTo={}) using candlestick path {}", emaHighPeriod, emaMidPeriod, emaLowPeriod, appliedTo, candlestickFile);

            LinkedList<BigDecimal> emaHighPrices = new LinkedList<>();
            LinkedList<BigDecimal> emaMidPrices = new LinkedList<>();
            LinkedList<BigDecimal> emaLowPrices = new LinkedList<>();
            BigDecimal[] emas = new BigDecimal[3];
            this.getCandlestickService().consumerCandlesticks(candlestickFile, candlestick -> {
                BigDecimal price = MathUtils.getDecimal(appliedTo.getPrice(candlestick));

                CompletableFuture<Void> threadHigh = CompletableFuture.runAsync(() -> EmasControllerImpl.calculateEma(emaHighPeriod, emaHighPrices, price, emas, 0), this.getExecutor());
                CompletableFuture<Void> threadMid = CompletableFuture.runAsync(() -> EmasControllerImpl.calculateEma(emaMidPeriod, emaMidPrices, price, emas, 1), this.getExecutor());
                CompletableFuture<Void> threadLow = CompletableFuture.runAsync(() -> EmasControllerImpl.calculateEma(emaLowPeriod, emaLowPrices, price, emas, 2), this.getExecutor());
                CompletableFuture.allOf(threadHigh, threadMid, threadLow).join();

                if (emas[0] != null && emas[1] != null && emas[2] != null) {
                    EMAsDto emAsDto = new EMAsDto(candlestick.getTimestamp(), emas[0], emas[1], emas[2]);
                    this.getEmasService().add(emAsDto);
                }
            });
            LocalDateTime end = LocalDateTime.now();
            log.info("EMAs(emaHighPeriod={},emaMidPeriod={},emaLowPeriod={},appliedTo={}) generate finished the async, duration {}", emaHighPeriod, emaMidPeriod, emaLowPeriod, appliedTo, MathUtils.formatDuration(start, end));
        };
    }
}
