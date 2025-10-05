package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.dto.TickDto;
import br.allandemiranda.fx.service.TickService;
import br.allandemiranda.fx.service.ValidateBean;
import br.allandemiranda.fx.utils.ReadFileDataUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Slf4j
@Service
@Validated
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TickServiceImpl implements TickService {

    private final ValidateBean validateBean;

    private static boolean isPriceEqualsAndUpdate(@Positive double price, @Positive double[] lastPrices, @PositiveOrZero int index) {
        boolean isPriceEquals = false;
        if (price >= 0D) {
            if (price == lastPrices[index]) {
                isPriceEquals = true;
            } else {
                lastPrices[index] = price;
            }
        }
        return isPriceEquals;
    }

    @Override
    public void consumerTicks(@Valid @NotNull Path tickFile, @Valid @NotNull Consumer<TickDto> consumer) {
        record TickFileLineModel(String date, String time, String bid, String ask) {
        }

        try (BufferedReader br = Files.newBufferedReader(tickFile, StandardCharsets.UTF_8)) {
            double[] lastPrices = new double[]{-1D, -1D};
            br.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split("\t"))
                    .filter(columns -> columns.length >= 4)
                    .map(columns -> new TickFileLineModel(columns[0], columns[1], columns[2], columns[3]))
                    .<TickDto>mapMulti((tick, out) -> {
                        LocalDateTime timestamp = ReadFileDataUtils.getDateTime(tick.date(), tick.time());
                        double bidPrice = ReadFileDataUtils.getPrice(tick.bid());
                        double askPrice = ReadFileDataUtils.getPrice(tick.ask());

                        boolean isBidEquals = TickServiceImpl.isPriceEqualsAndUpdate(bidPrice, lastPrices, 0);
                        boolean isAskEquals = TickServiceImpl.isPriceEqualsAndUpdate(askPrice, lastPrices, 1);

                        if (!isBidEquals && !isAskEquals && lastPrices[0] >= 0D && lastPrices[1] >= 0D) {
                            TickDto tickDto = new TickDto(timestamp, lastPrices[0], lastPrices[1]);
                            this.getValidateBean().validate(tickDto, timestamp);
                            out.accept(tickDto);
                        }
                    }).forEachOrdered(consumer);
        } catch (Exception e) {
           log.warn(e.getMessage());
        }
    }
}
