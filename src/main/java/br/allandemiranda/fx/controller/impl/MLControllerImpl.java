package br.allandemiranda.fx.controller.impl;

import br.allandemiranda.fx.dto.EMAsDto;
import br.allandemiranda.fx.dto.GarchDto;
import br.allandemiranda.fx.dto.GarchPositionTypeDto;
import br.allandemiranda.fx.dto.GarchTradingDto;
import br.allandemiranda.fx.dto.MACDDto;
import br.allandemiranda.fx.dto.RSIDto;
import br.allandemiranda.fx.enums.DealReason;
import br.allandemiranda.fx.enums.XGBLabel;
import br.allandemiranda.fx.service.CandlestickService;
import br.allandemiranda.fx.service.EmasService;
import br.allandemiranda.fx.service.GarchService;
import br.allandemiranda.fx.service.GarchTradingService;
import br.allandemiranda.fx.service.MacdService;
import br.allandemiranda.fx.service.RsiService;
import br.allandemiranda.fx.service.ValidateBean;
import br.allandemiranda.fx.service.XgbService;
import br.allandemiranda.fx.utils.MarketSessionUtils;
import br.allandemiranda.fx.utils.MathUtils;
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
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
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
    private final XgbService xgbService;
    private final Executor executor;
    private final ValidateBean validateBean;

    private static float @NotNull [] getDataSet(@NotNull LocalDateTime timestamp, @NotNull EMAsDto emas, @NotNull MACDDto macd, @NotNull RSIDto rsi, @NotNull GarchDto garch, @NotNull GarchTradingDto trading) {
        // time indicators
        float dayOfWeek = timestamp.getDayOfWeek().getValue();
        float session = MarketSessionUtils.detectSession(timestamp).getValue();

        // EMAs indicator
        float emaHigh = emas.getEmaHigh().floatValue();
        float emaMid = emas.getEmaMid().floatValue();
        float emaLow = emas.getEmaLow().floatValue();

        // MACD indicator
        float macdV = macd.getMacd().floatValue();
        float signal = macd.getSignal().floatValue();

        // RSI indicator
        float rsiV = rsi.getRsi().floatValue();

        // Garch
        float sigmaPerBarPips = (float) garch.getSigmaPerBarPips();
        float mu = (float) garch.getMu();
        float omega = (float) garch.getOmega();
        float alpha = (float) garch.getAlpha();
        float beta = (float) garch.getBeta();
        float nu = (float) garch.getNu();

        // Garch Trading
        float spread = (float) trading.getSpread();

        // data set
        return new float[]{dayOfWeek, session, emaHigh, emaMid, emaLow, macdV, signal, rsiV, sigmaPerBarPips, mu, omega, alpha, beta, nu, spread};
    }

    private static XGBLabel getLabelByGarchTrading(@NotNull GarchTradingDto trading) {
        GarchPositionTypeDto buy = trading.getPositionTypeBuy();
        GarchPositionTypeDto sell = trading.getPositionTypeSell();

        if (buy.getDealReason().equals(DealReason.DEAL_REASON_TP)) {
            if (sell.getDealReason().equals(DealReason.DEAL_REASON_TP)) {
                if (buy.getCloseTime().isBefore(sell.getCloseTime())) {
                    return XGBLabel.BUY;
                } else if (buy.getCloseTime().isAfter(sell.getCloseTime())) {
                    return XGBLabel.SELL;
                } else if (buy.getProfit() + buy.getSwapCost() < sell.getProfit() + sell.getSwapCost()) {
                    return XGBLabel.BUY;
                } else if (buy.getProfit() + buy.getSwapCost() > sell.getProfit() + sell.getSwapCost()) {
                    return XGBLabel.SELL;
                } else {
                    return XGBLabel.NEUTRAL;
                }
            } else {
                return XGBLabel.BUY;
            }
        } else if (sell.getDealReason().equals(DealReason.DEAL_REASON_TP)) {
            return XGBLabel.SELL;
        } else {
            return XGBLabel.NEUTRAL;
        }
    }

    @PostMapping
    public ResponseEntity<Void> generateML(@RequestParam @Valid @NotNull Path candlesticksFile, @RequestParam @NotNull @Valid Path saveLocation) {
        this.getRunnableJob(candlesticksFile, saveLocation);
        return ResponseEntity.ok().build();
    }

    private void getRunnableJob(@NotNull Path candlesticksFile, @NotNull Path saveLocation) {
        this.getExecutor().execute(() -> {
            LocalDateTime start = LocalDateTime.now();
            log.info("Start generate Data Model to XGB");
            AbstractMap.SimpleEntry<float[][], float[]> dataModel = this.getDataModel(candlesticksFile);
            log.info("Finish generate Data Model to XGB, duration {}", MathUtils.formatDuration(start, LocalDateTime.now()));
            this.getXgbService().runTrainer(dataModel.getKey(), dataModel.getValue());
            this.getXgbService().saveTrainer(saveLocation);
        });
    }

    private AbstractMap.SimpleEntry<float[][], float[]> getDataModel(@NotNull Path candlesticksFile) {
        List<AbstractMap.SimpleEntry<float[], Float>> dataModel = new LinkedList<>();
        record Data(@PastOrPresent LocalDateTime timestamp, EMAsDto emas, MACDDto macd, RSIDto rsi, GarchDto garch, GarchTradingDto trading) {
            public boolean isValid() {
                return emas() != null && macd() != null && rsi() != null && garch() != null && trading() != null;
            }
        }

        LinkedList<Data> lastThree = new LinkedList<>();
        this.getCandlestickService().consumerCandlesticks(candlesticksFile, candlestick -> {
            LocalDateTime timestamp = candlestick.getTimestamp();
            EMAsDto emas = this.getEmasService().getEmas(timestamp).orElse(null);
            MACDDto macd = this.getMacdService().getMACD(timestamp).orElse(null);
            RSIDto rsi = this.getRsiService().getRSI(timestamp).orElse(null);
            GarchDto garch = this.getGarchService().getGarch(timestamp).orElse(null);
            GarchTradingDto trading = this.getGarchTradingService().getGarchTrading(timestamp).orElse(null);
            Data data = new Data(timestamp, emas, macd, rsi, garch, trading);
            lastThree.addLast(data);
            while (lastThree.size() > 3) {
                lastThree.removeFirst();
            }

            if (lastThree.size() == 3 && lastThree.stream().allMatch(Data::isValid)) {
                List<float[]> rows = lastThree.stream()
                        .map(d -> MLControllerImpl.getDataSet(d.timestamp, d.emas, d.macd, d.rsi, d.garch, d.trading))
                        .toList();

                int total = rows.stream().mapToInt(a -> a.length).sum();
                float[] flat = new float[total];

                int pos = 0;
                for (float[] r : rows) {
                    System.arraycopy(r, 0, flat, pos, r.length);
                    pos += r.length;
                }

                assert trading != null;
                dataModel.add(new AbstractMap.SimpleEntry<>(flat, (float) MLControllerImpl.getLabelByGarchTrading(trading).getValue()));
            }
        });

        int n = dataModel.size();
        float[][] x = new float[n][];
        float[] y = new float[n];

        int i = 0;
        for (var e : dataModel) {
            x[i] = e.getKey();
            y[i] = e.getValue();
            i++;
        }

        return new AbstractMap.SimpleEntry<>(x, y);
    }
}
