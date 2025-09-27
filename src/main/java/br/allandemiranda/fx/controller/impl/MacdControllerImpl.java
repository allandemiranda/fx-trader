package br.allandemiranda.fx.controller.impl;

import br.allandemiranda.fx.controller.MacdController;
import br.allandemiranda.fx.dto.MACDDto;
import br.allandemiranda.fx.enums.AppliedPrice;
import br.allandemiranda.fx.service.CandlestickService;
import br.allandemiranda.fx.service.MacdService;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/macd")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MacdControllerImpl implements MacdController {

    private final Executor executor;
    private final CandlestickService candlestickService;
    private final MacdService macdService;

    private static void calculateEma(@NotNull LinkedList<BigDecimal> prices, int period, BigDecimal[] emas, int x, BigDecimal currentPrice) {
        if (prices.size() >= period) {
            if (emas[x] == null) {
                List<BigDecimal> lastPrices = MathUtils.lastN(prices, period);
                emas[x] = MovingAverageUtils.getSimpleMovingAverage(lastPrices, period);
            } else {
                emas[x] = MovingAverageUtils.getExponentialMovingAverage(emas[x], currentPrice, period);
            }
        }
    }

    @PostMapping
    @Override
    public ResponseEntity<Void> generateAllMacd(@RequestParam @Valid @NotNull Path candlesticksFile, @RequestParam @Valid @Min(2) int fastEmaPeriod, @RequestParam @Valid @Min(2) int slowEmaPeriod, @RequestParam @Valid @Min(2) int macdEmaPeriod, @RequestParam @Valid @NotNull AppliedPrice appliedTo) {
        this.getExecutor().execute(() -> this.getRunnableJob(candlesticksFile, fastEmaPeriod, slowEmaPeriod, macdEmaPeriod, appliedTo));
        return ResponseEntity.ok().build();
    }

    protected void getRunnableJob(@NotNull Path candlesticksFile, int fastEmaPeriod, int slowEmaPeriod, int macdEmaPeriod, @NotNull AppliedPrice appliedTo) {
            LocalDateTime start = LocalDateTime.now();
            log.info("Generate all MACD({},{},{})[{}] using candlestick path {}", fastEmaPeriod, slowEmaPeriod, macdEmaPeriod, appliedTo, candlesticksFile);

            LinkedList<BigDecimal> prices = new LinkedList<>();
            BigDecimal[] emas = new BigDecimal[3];
            LinkedList<BigDecimal> macds = new LinkedList<>();
            this.getCandlestickService().consumerCandlesticks(candlesticksFile, candlestick -> {
                BigDecimal currentPrice = MathUtils.getDecimal(appliedTo.getPrice(candlestick));

                if (emas[0] == null || emas[1] == null) {
                    prices.addLast(currentPrice);
                }

                CompletableFuture<Void> emaFast = CompletableFuture.runAsync(() -> MacdControllerImpl.calculateEma(prices, fastEmaPeriod, emas, 0, currentPrice), this.getExecutor());
                CompletableFuture<Void> emaSlow = CompletableFuture.runAsync(() -> MacdControllerImpl.calculateEma(prices, slowEmaPeriod, emas, 1, currentPrice), this.getExecutor());
                CompletableFuture.allOf(emaFast, emaSlow).join();

                if (emas[0] != null && emas[1] != null) {
                    BigDecimal macd = emas[0].subtract(emas[1]);

                    if (emas[2] == null) {
                        macds.addLast(macd);
                        if (macds.size() == macdEmaPeriod) {
                            emas[2] = MovingAverageUtils.getSimpleMovingAverage(macds, macdEmaPeriod);
                            macds.clear();
                        }
                    } else {
                        emas[2] = MovingAverageUtils.getExponentialMovingAverage(emas[2], macd, macdEmaPeriod);
                    }

                    if (emas[2] != null) {
                        MACDDto macdDto = new MACDDto(candlestick.getTimestamp(), macd, emas[2]);
                        this.getMacdService().addMACD(macdDto);
                    }
                }
            });
            LocalDateTime end = LocalDateTime.now();
            log.info("MACD({},{},{})[{}] generate finished, duration {}", fastEmaPeriod, slowEmaPeriod, macdEmaPeriod, appliedTo, MathUtils.formatDuration(start, end));
    }
}
