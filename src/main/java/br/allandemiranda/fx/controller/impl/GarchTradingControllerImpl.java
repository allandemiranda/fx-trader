package br.allandemiranda.fx.controller.impl;

import br.allandemiranda.fx.controller.GarchTradingController;
import br.allandemiranda.fx.dto.GarchPositionTypeDto;
import br.allandemiranda.fx.dto.GarchTradingDto;
import br.allandemiranda.fx.dto.TickDto;
import br.allandemiranda.fx.enums.DealReason;
import br.allandemiranda.fx.enums.Timeframe;
import br.allandemiranda.fx.service.GarchService;
import br.allandemiranda.fx.service.GarchTradingService;
import br.allandemiranda.fx.service.TickService;
import br.allandemiranda.fx.service.ValidateBean;
import br.allandemiranda.fx.utils.MathUtils;
import br.allandemiranda.fx.utils.SwapUtils;
import br.allandemiranda.fx.utils.TimeframeUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/garchTrading")
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class GarchTradingControllerImpl implements GarchTradingController {

    private final TickService tickService;
    private final GarchTradingService garchTradingService;
    private final GarchService garchService;
    private final Executor executor;
    private final ValidateBean validateBean;

    private static @NotNull GarchPositionTypeDto updateGarchTrading(boolean isBuy, @NotNull GarchPositionTypeDto garchPosition, @NotNull TickDto tick, double swapLongPts, double swapShortPts, @PositiveOrZero int rollover, @Positive double tp, @Negative double sl) {
        if (garchPosition.getDealReason().equals(DealReason.NONE)) {
            double openPrice = garchPosition.getOpenPrice();
            double profit = isBuy ? GarchTradingControllerImpl.getBuyProfit(openPrice, tick) : GarchTradingControllerImpl.getSellProfit(openPrice, tick);
            double swapCost = SwapUtils.calcSwapInPoints(isBuy, swapLongPts, swapShortPts, rollover);
            GarchPositionTypeDto updated = garchPosition.toBuilder().closeTime(tick.getTimestamp()).profit(profit).swapCost(swapCost).build();
            if (updated.getProfit() >= tp) {
                return updated.toBuilder().dealReason(DealReason.DEAL_REASON_TP).build();
            } else if (updated.getProfit() <= sl) {
                return updated.toBuilder().dealReason(DealReason.DEAL_REASON_SL).build();
            } else if (updated.getSwapCost() <= -Math.abs(tp)) {
                return updated.toBuilder().dealReason(DealReason.DEAL_REASON_CORPORATE_ACTION).build();
            } else {
                return updated;
            }
        } else {
            return garchPosition;
        }
    }

    private static double getBuyProfit(@Positive double openPrice, @NotNull TickDto tick) {
        return tick.getBid() - openPrice;
    }

    private static double getSellProfit(@Positive double openPrice, @NotNull TickDto tick) {
        return openPrice - tick.getAsk();
    }

    @PostMapping
    @Override
    public ResponseEntity<Void> generateAllGarchTrading(@RequestParam @Valid @NotNull Path ticksFile, @RequestParam @Valid @NotNull Timeframe timeframe, @RequestParam @Valid @NotNull DayOfWeek tripleDay, @RequestParam double swapLongPts, @RequestParam double swapShortPts, @RequestParam double pipSize) {
        this.getExecutor().execute(() -> this.getRunnableJob(ticksFile, timeframe, tripleDay, swapLongPts, swapShortPts, pipSize));
        return ResponseEntity.ok().build();
    }

    protected void getRunnableJob(@NotNull Path ticksFile, @NotNull Timeframe timeframe, @NotNull DayOfWeek tripleDay, double swapLongPts, double swapShortPts, double pipSize) {
        LinkedList<TickDto> list = new LinkedList<>();
        System.out.println("START!");
        final int[] time = new int[]{0};
        this.getTickService().consumerTicks(ticksFile, tick -> {
            if(tick.getTimestamp().getMonthValue() != time[0]) {
                time[0] =  tick.getTimestamp().getMonthValue();
                System.out.println(tick.getTimestamp().toLocalDate());
            }

            list.addLast(tick);
            if(list.size() > 20) {
                list.removeFirst();

                if(!this.getValidateBean().isValidate(list.get(9), list.get(9).getTimestamp())) {
                    log.info("----");
                    list.forEach(t -> log.info("[timestamp: {}, bid: {}, ask: {}]", t.getTimestamp(), t.getBid(), t.getAsk()));
                    log.info("----");
                }
            }
        });
        System.out.println("END!");
//        LocalDateTime start = LocalDateTime.now();
//        log.info("Generate all Garch Trading(timeframe={},tripleDay={},swapLongPts={},swapShortPts={}) using tick path {}", timeframe, tripleDay, swapLongPts, swapShortPts, ticksFile);
//
//        int[] cont = new int[]{0, 0};
//
//        TickDto[] lastTick = new TickDto[1];
//        ConcurrentHashMap<LocalDateTime, Trading> openPositions = new ConcurrentHashMap<>();
//        ConcurrentLinkedDeque<CompletableFuture<Void>> runnable = new ConcurrentLinkedDeque<>();
//        this.getTickService().consumerTicks(ticksFile, tick -> {
//            if (lastTick[0] != null) {
//
//                //5760
//                if (openPositions.size() >= 5760) {
//                    ConcurrentHashMap<LocalDateTime, Trading> openPositionsTmp = new ConcurrentHashMap<>(openPositions);
//                    openPositions.clear();
//                    runnable.add(CompletableFuture.runAsync(() -> {
//                        log.info("Starting cont [{}, {}]", ++cont[0], cont[1]);
//                        extracted(ticksFile, tripleDay, swapLongPts, swapShortPts, openPositionsTmp);
//                        log.info("Ending cont [{}, {}]", cont[0], ++cont[1]);
//                    }, this.getExecutor()));
//
//                }
//
//                LocalDateTime lastOpenTime = TimeframeUtils.getCandlestickTimestamp(lastTick[0].getTimestamp(), timeframe);
//                LocalDateTime currentOpenTime = TimeframeUtils.getCandlestickTimestamp(tick.getTimestamp(), timeframe);
//                if (lastOpenTime.isBefore(currentOpenTime)) {
//                    this.getGarchService().getGarch(currentOpenTime).ifPresent(garch -> {
//                        double spread = tick.getAsk() - tick.getBid();
//                        double tp = garch.getTpPips() * pipSize;
//                        double sl = -Math.abs(garch.getSlPips()) * pipSize;
//
//                        DealReason dealReason = spread >= Math.abs(garch.getSlPips()) ? DealReason.DEAL_REASON_SL : DealReason.NONE;
//
//                        GarchPositionTypeDto buy = new GarchPositionTypeDto(tick.getTimestamp(), Math.abs(spread), 0, dealReason, tick.getAsk());
//                        GarchPositionTypeDto sell = new GarchPositionTypeDto(tick.getTimestamp(), Math.abs(spread), 0, dealReason, tick.getBid());
//
//                        if (dealReason.equals(DealReason.DEAL_REASON_SL)) {
//                            GarchTradingDto garchTradingDto = new GarchTradingDto(garch.getTimestamp(), tick.getTimestamp(), buy, sell, spread);
//                            this.getGarchTradingService().addGarchTrading(garchTradingDto);
//                        } else {
//                            Trading trading = new Trading(tick.getTimestamp(), buy, sell, spread, tp, sl);
//                            this.getValidateBean().validate(trading, tick.getTimestamp());
//                            openPositions.put(currentOpenTime, trading);
//                        }
//                    });
//                }
//
//            }
//            lastTick[0] = tick;
//        });
//        log.warn("Waiting for join runnable {} -> duration {}", runnable.size(), MathUtils.formatDuration(start, LocalDateTime.now()));
//        runnable.forEach(CompletableFuture::join);
//        log.info("Garch Trading(timeframe={},tripleDay={},swapLongPts={},swapShortPts={}) generate finished, duration {}", timeframe, tripleDay, swapLongPts, swapShortPts, MathUtils.formatDuration(start, LocalDateTime.now()));
    }

    private void extracted(Path ticksFile, DayOfWeek tripleDay, double swapLongPts, double swapShortPts, ConcurrentHashMap<LocalDateTime, Trading> openPositions) {
        LocalDateTime openTime = openPositions.values().stream().map(Trading::openTime).min(LocalDateTime::compareTo).orElseThrow();
        LocalDateTime closeTime = openPositions.values().stream().map(Trading::openTime).max(LocalDateTime::compareTo).orElseThrow();
        log.info("Reading block tick start {} -> {}", openTime, closeTime);
        LocalDateTime start = LocalDateTime.now();
        this.getTickService().consumerTicks(ticksFile, tick -> {
            if (tick.getTimestamp().isAfter(openTime)) {
                if (!openPositions.isEmpty()) {
                    openPositions.entrySet().parallelStream().filter(ent -> tick.getTimestamp().isAfter(ent.getValue().openTime())).forEach(position -> {
                        LocalDateTime timestamp = position.getKey();
                        Trading trading = position.getValue();
                        int rollover = SwapUtils.countRolloverUnits(trading.openTime, tick.getTimestamp(), tripleDay);

                        GarchPositionTypeDto buy = GarchTradingControllerImpl.updateGarchTrading(true, trading.buy(), tick, swapLongPts, swapShortPts, rollover, trading.tp, trading.sl);
                        this.getValidateBean().validate(buy, trading.openTime);
                        GarchPositionTypeDto sell = GarchTradingControllerImpl.updateGarchTrading(false, trading.sell(), tick, swapLongPts, swapShortPts, rollover, trading.tp, trading.sl);
                        this.getValidateBean().validate(sell, trading.openTime);
                        Trading tradingUpdated = new Trading(trading.openTime, buy, sell, trading.spread, trading.tp, trading.sl);
                        this.getValidateBean().validate(tradingUpdated, tradingUpdated.openTime);

                        if (tradingUpdated.buy().getDealReason().equals(DealReason.DEAL_REASON_TP) || tradingUpdated.sell().getDealReason().equals(DealReason.DEAL_REASON_TP)) {
                            GarchTradingDto garchTradingDto = new GarchTradingDto(timestamp, trading.openTime, buy, sell, trading.spread);
                            this.getGarchTradingService().addGarchTrading(garchTradingDto);
                            openPositions.remove(timestamp);
                        } else if (!tradingUpdated.buy().getDealReason().equals(DealReason.NONE) && !tradingUpdated.sell().getDealReason().equals(DealReason.NONE)) {
                            GarchTradingDto garchTradingDto = new GarchTradingDto(timestamp, trading.openTime, buy, sell, trading.spread);
                            this.getGarchTradingService().addGarchTrading(garchTradingDto);
                            openPositions.remove(timestamp);
                        } else {
                            openPositions.put(timestamp, tradingUpdated);
                        }
                    });
                } else {
                    throw new RuntimeException("Stopping ticks consumer");
                }
            }
        });
        log.info("-----------------------> End block tick start {} -> {}, duration {}", openTime, closeTime, MathUtils.formatDuration(start, LocalDateTime.now()));
    }

    record Trading(@NotNull @PastOrPresent LocalDateTime openTime, @NotNull GarchPositionTypeDto buy, @NotNull GarchPositionTypeDto sell, @PositiveOrZero double spread, @Positive double tp, @Negative double sl) {
    }
}
