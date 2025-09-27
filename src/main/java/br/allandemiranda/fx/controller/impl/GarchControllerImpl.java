package br.allandemiranda.fx.controller.impl;

import br.allandemiranda.fx.controller.GarchController;
import br.allandemiranda.fx.dto.GarchDto;
import br.allandemiranda.fx.enums.AppliedPrice;
import br.allandemiranda.fx.service.CandlestickService;
import br.allandemiranda.fx.service.GarchService;
import br.allandemiranda.fx.share.GarchComputing;
import br.allandemiranda.fx.utils.MathUtils;
import jakarta.validation.Valid;
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

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/garch")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GarchControllerImpl implements GarchController {

    private final GarchService garchService;
    private final CandlestickService candlestickService;
    private final Executor executor;

    @PostMapping
    @Override
    public ResponseEntity<Void> generateAllGarch(@RequestParam @Valid @NotNull Path candlestickFile, @RequestParam int sampleSize, @RequestParam @Valid @NotNull AppliedPrice appliedTo, @RequestParam double pipSize, @RequestParam int horizonBars, @RequestParam double alphaTP, @RequestParam double alphaSL) {
        this.getExecutor().execute(this.getRunnableJob(candlestickFile, sampleSize, appliedTo, pipSize, horizonBars, alphaTP, alphaSL));
        return ResponseEntity.ok().build();
    }

    protected @NotNull Runnable getRunnableJob(@NotNull Path candlestickFile, int sampleSize, @NotNull AppliedPrice appliedTo, double pipSize, int horizonBars, double alphaTP, double alphaSL) {
        return () -> {
            LocalDateTime start = LocalDateTime.now();
            log.info("Generate all Garch(sampleSize={},appliedTo={},pipSize={},horizonBars={},alphaTP={},alphaSL={}) using candlestick path {}", sampleSize, appliedTo, pipSize, horizonBars, alphaTP, alphaSL, candlestickFile);
            LinkedList<Double> prices = new LinkedList<>();
            Collection<CompletableFuture<Void>> futures = new LinkedList<>();
            this.getCandlestickService().consumerCandlesticks(candlestickFile, candlestick -> {
                double currentPrice = appliedTo.getPrice(candlestick);
                prices.addLast(currentPrice);
                while (prices.size() > sampleSize) {
                    prices.removeFirst();
                }
                if (prices.size() == sampleSize) {
                    final LocalDateTime candlestickTimestamp = LocalDateTime.of(candlestick.getTimestamp().toLocalDate(), candlestick.getTimestamp().toLocalTime());
                    final List<Double> pricesGarch = new ArrayList<>(prices);
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        GarchDto garchDto = new GarchComputing().computer(candlestickTimestamp, pricesGarch, pipSize, horizonBars, alphaTP, alphaSL).orElseThrow();
                        this.getGarchService().addGarch(garchDto);
                    }, this.getExecutor());
                    futures.add(future);
                }
            });
            log.info("Running {} jobs for calculate Garch, time generating jobs {}", futures.size(), MathUtils.formatDuration(start, LocalDateTime.now()));
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            LocalDateTime end = LocalDateTime.now();
            log.info("Garch(sampleSize={},appliedTo={},pipSize={},horizonBars={},alphaTP={},alphaSL={}) generate finished the async, duration {}", sampleSize, appliedTo, pipSize, horizonBars, alphaTP, alphaSL, MathUtils.formatDuration(start, end));
        };
    }
}
