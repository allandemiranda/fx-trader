package br.allandemiranda.fx.controller.impl;

import br.allandemiranda.fx.controller.RsiController;
import br.allandemiranda.fx.dto.RSIDto;
import br.allandemiranda.fx.enums.AppliedPrice;
import br.allandemiranda.fx.service.CandlestickService;
import br.allandemiranda.fx.service.RsiService;
import br.allandemiranda.fx.service.ValidateBean;
import br.allandemiranda.fx.utils.MathUtils;
import br.allandemiranda.fx.utils.MovingAverageUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
@RequestMapping("/api/rsi")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RsiControllerImpl implements RsiController {

    private final Executor executor;
    private final CandlestickService candlestickService;
    private final RsiService rsiService;
    private final ValidateBean validateBean;

    @PostMapping
    @Override
    public ResponseEntity<Void> generateAllRsi(@RequestParam @Valid @NotNull Path candlesticksFile, @RequestParam @Valid @Min(2) int period, @RequestParam @Valid @NotNull AppliedPrice appliedTo) {
        this.getExecutor().execute(() -> this.getRunnableJob(candlesticksFile, period, appliedTo));
        return ResponseEntity.ok().build();
    }

    protected void getRunnableJob(@NotNull Path candlesticksFile, int period, @NotNull AppliedPrice appliedTo) {
            LocalDateTime start = LocalDateTime.now();
            log.info("Generate all RSI({})[{}] using candlestick path {}", period, appliedTo, candlesticksFile);

            record Price(@NotNull @Positive BigDecimal price, @NotNull @PositiveOrZero BigDecimal gain, @NotNull @PositiveOrZero BigDecimal loss) {
                public static @NotNull Price getPrice(@NotNull BigDecimal currentPrice, @NotNull Price lastPrice) {
                    BigDecimal gain = currentPrice.compareTo(lastPrice.price()) > 0 ? currentPrice.subtract(lastPrice.price()) : BigDecimal.ZERO;
                    BigDecimal loss = currentPrice.compareTo(lastPrice.price()) < 0 ? lastPrice.price().subtract(currentPrice) : BigDecimal.ZERO;
                    return new Price(currentPrice, gain, loss);
                }
            }
            LinkedList<Price> priceList = new LinkedList<>();
            this.getCandlestickService().consumerCandlesticks(candlesticksFile, candlestick -> {
                BigDecimal currentPrice = MathUtils.getDecimal(appliedTo.getPrice(candlestick));
                Price lastPrice = priceList.isEmpty() ? null : priceList.getLast();
                LocalDateTime timestamp = candlestick.getTimestamp();
                if (priceList.isEmpty()) {
                    Price price = new Price(currentPrice, BigDecimal.ZERO, BigDecimal.ZERO);
                    this.getValidateBean().validate(price, timestamp);
                    priceList.add(price);
                } else if (priceList.size() == 1 && lastPrice.gain().compareTo(BigDecimal.ZERO) == 0 && lastPrice.loss().compareTo(BigDecimal.ZERO) == 0) {
                    Price price = Price.getPrice(currentPrice, lastPrice);
                    this.getValidateBean().validate(price, timestamp);
                    priceList.clear();
                    priceList.addLast(price);
                } else {
                    Price price = Price.getPrice(currentPrice, lastPrice);
                    this.getValidateBean().validate(price, timestamp);
                    priceList.addLast(price);
                    while (priceList.size() > period) {
                        priceList.removeFirst();
                    }

                    CompletableFuture<BigDecimal> averageGain = CompletableFuture.supplyAsync(() -> {
                        List<BigDecimal> gains = priceList.stream().map(Price::gain).toList();
                        return MovingAverageUtils.getSimpleMovingAverage(gains, period);
                    }, this.getExecutor());
                    CompletableFuture<BigDecimal> averageLoss = CompletableFuture.supplyAsync(() -> {
                        List<BigDecimal> losses = priceList.stream().map(Price::loss).toList();
                        return MovingAverageUtils.getSimpleMovingAverage(losses, period);
                    }, this.getExecutor());
                    CompletableFuture.allOf(averageGain, averageLoss).join();

                    BigDecimal hundred = BigDecimal.valueOf(100D);
                    BigDecimal averageLossValue = averageLoss.join();
                    BigDecimal averageGainValue = averageGain.join();
                    BigDecimal rs = (averageLossValue.compareTo(BigDecimal.ZERO) == 0) ? hundred : averageGainValue.divide(averageLossValue, MathUtils.PRECISION);
                    BigDecimal rsi = (rs.compareTo(hundred) == 0) ? hundred : hundred.subtract(hundred.divide(BigDecimal.ONE.add(rs), MathUtils.PRECISION));

                    RSIDto rsiDto = new RSIDto(timestamp, rsi);
                    this.getValidateBean().validate(rsiDto, timestamp);
                    this.getRsiService().addRSI(rsiDto);
                }
            });
            LocalDateTime end = LocalDateTime.now();
            log.info("RSI({})[{}] generate finished, duration {}", period, appliedTo, MathUtils.formatDuration(start, end));
    }
}
