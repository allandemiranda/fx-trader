package br.allandemiranda.fx.controller.impl;

import br.allandemiranda.fx.controller.BatchJobController;
import br.allandemiranda.fx.enums.AppliedPrice;
import br.allandemiranda.fx.enums.Timeframe;
import br.allandemiranda.fx.utils.MathUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BatchJobControllerImpl implements BatchJobController {
    // TODO: TEST CLASS

    // GROBAL
    public static final Path CANDLESTICK_H1_FILE = Path.of("C:\\Users\\allan\\OneDrive\\Documentos\\MT5\\EURUSD_H1_200001030000_202509122300.csv");
    public static final Path TICKS_FILE = Path.of("C:\\Users\\allan\\OneDrive\\Documentos\\MT5\\EURUSD_201112190000_202509122358.csv");
    public static final AppliedPrice APPLIED_PRICE = AppliedPrice.PRICE_CLOSE;
    public static final Timeframe TIMEFRAME = Timeframe.H1;

    // EMAs
    public static final int EMAS_EMA_HIGH_PERIOD = 20;
    public static final int EMAS_EMA_MID_PERIOD = 50;
    public static final int EMAS_EMA_LOW_PERIOD = 200;

    // GARCH
    public static final int SAMPLE_SIZE = 5000;
    public static final double PIP_SIZE = 0.0001;
    public static final int HORIZON_BARS = 3;
    public static final double ALPHA_TP = 0.012;
    public static final double ALPHA_SL = 0.05;

    // GARCH TRADING
    public static final DayOfWeek TRIPLE_DAY = DayOfWeek.WEDNESDAY;
    public static final double SWAP_LONG_PTS = -5.46;
    public static final double SWAP_SHORT_PTS = 3.07;

    // MACD
    public static final int MACD_FAST_EMA_PERIOD = 12;
    public static final int MACD_SLOW_EMA_PERIOD = 26;
    public static final int MACD_MACD_EMA_PERIOD = 9;

    // RIS
    public static final int RSI_PERIOD = 14;

    private final EmasControllerImpl emasController;
    private final GarchControllerImpl garchController;
    private final GarchTradingControllerImpl garchTradingController;
    private final MacdControllerImpl macdController;
    private final RsiControllerImpl rsiController;

    private final Executor executor;

    @GetMapping
    @Override
    public ResponseEntity<Void> runAllJobsWithDefaultInputs() {
        this.getExecutor().execute(() -> {
            LocalDateTime start = LocalDateTime.now();
            log.info("Starting the bach jobs");

            CompletableFuture<Void> emas = CompletableFuture.runAsync(() -> this.getEmasController().getRunnableJob(CANDLESTICK_H1_FILE, EMAS_EMA_HIGH_PERIOD, EMAS_EMA_MID_PERIOD, EMAS_EMA_LOW_PERIOD, APPLIED_PRICE), this.getExecutor());
            CompletableFuture<Void> garchAndTrading = CompletableFuture.runAsync(() -> {
                this.getGarchController().getRunnableJob(CANDLESTICK_H1_FILE, SAMPLE_SIZE, APPLIED_PRICE, PIP_SIZE, HORIZON_BARS, ALPHA_TP, ALPHA_SL);
                this.getGarchTradingController().getRunnableJob(TICKS_FILE, TIMEFRAME, TRIPLE_DAY, SWAP_LONG_PTS, SWAP_SHORT_PTS);
            }, this.getExecutor());
            CompletableFuture<Void> macd = CompletableFuture.runAsync(() -> this.getMacdController().generateAllMacd(CANDLESTICK_H1_FILE, MACD_FAST_EMA_PERIOD, MACD_SLOW_EMA_PERIOD, MACD_MACD_EMA_PERIOD, APPLIED_PRICE), this.getExecutor());
            CompletableFuture<Void> rsi = CompletableFuture.runAsync(() -> this.getRsiController().getRunnableJob(CANDLESTICK_H1_FILE, RSI_PERIOD, APPLIED_PRICE), this.getExecutor());

            CompletableFuture.allOf(emas, garchAndTrading, macd, rsi).join();
            LocalDateTime end = LocalDateTime.now();
            log.info("Ending the bach jobs, duration {}", MathUtils.formatDuration(start, end));
        });
        return ResponseEntity.ok().build();
    }
}
