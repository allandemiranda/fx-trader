package br.allandemiranda.fx.utils;

import br.allandemiranda.fx.dto.CandlestickDto;
import br.allandemiranda.fx.dto.TickDto;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.function.Consumer;

@UtilityClass
public final class ReadFileDataUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").optionalStart().appendPattern(".SSS").optionalEnd().toFormatter();

    private static @NotNull LocalDateTime getDateTime(String date, String time) {
        LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
        LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
        return LocalDateTime.of(localDate, localTime);
    }

    private static @Nullable BigDecimal getPrice(String price) {
        if (price.isBlank()) {
            return null;
        } else {
            return BigDecimal.valueOf(Double.parseDouble(price));
        }
    }

    @SneakyThrows
    public static void readTicksFile(Path tickFile, Consumer<TickDto> saveTick) {
        try (BufferedReader br = Files.newBufferedReader(tickFile, StandardCharsets.UTF_8)) {
            BigDecimal[] lastPrices = new BigDecimal[2];
            br.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split("\t"))
                    .filter(columns -> columns.length > 4)
                    .map(columns -> new TickFileLineModel(columns[0], columns[1], columns[2], columns[3]))
                    .<TickDto>mapMulti((tick, consumer) -> {
                        LocalDateTime dateTime = ReadFileDataUtils.getDateTime(tick.date(), tick.time());
                        BigDecimal bidPrice = ReadFileDataUtils.getPrice(tick.bid());
                        BigDecimal askPrice = ReadFileDataUtils.getPrice(tick.ask());

                        boolean isBidEquals = isPriceEqualsAndUpdate(bidPrice, lastPrices, 0);
                        boolean isAskEquals = isPriceEqualsAndUpdate(askPrice, lastPrices, 1);

                        if (!isBidEquals && !isAskEquals && lastPrices[0] != null && lastPrices[1] != null) {
                            consumer.accept(new TickDto(dateTime, lastPrices[0], lastPrices[1]));
                        }
                    }).forEach(saveTick);
        }
    }

    private static boolean isPriceEqualsAndUpdate(BigDecimal price, BigDecimal[] lastPrices, int index) {
        boolean isPriceEquals = false;
        if (price != null) {
            if (lastPrices[index] != null && price.compareTo(lastPrices[index]) == 0) {
                isPriceEquals = true;
            } else {
                lastPrices[index] = price;
            }
        }
        return isPriceEquals;
    }

    @SneakyThrows
    public static void readCandlestickFile(Path candlestickFile, Consumer<CandlestickDto> saveCandlestick) {
        try (BufferedReader br = Files.newBufferedReader(candlestickFile, StandardCharsets.UTF_8)) {
            br.lines()
                    .skip(1)
                    .parallel()
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split("\t"))
                    .filter(columns -> columns.length > 6)
                    .map(columns -> new CandlestickFileLineModel(columns[0], columns[1], columns[2], columns[3], columns[4], columns[5]))
                    .map(candlestick -> {
                        LocalDateTime openDateTime = ReadFileDataUtils.getDateTime(candlestick.openDate(), candlestick.openTime());
                        return new CandlestickDto(openDateTime, ReadFileDataUtils.getPrice(candlestick.open()), ReadFileDataUtils.getPrice(candlestick.high()), ReadFileDataUtils.getPrice(candlestick.low()), ReadFileDataUtils.getPrice(candlestick.close()), null);
                    }).forEach(saveCandlestick);
        }
    }

    private record TickFileLineModel(String date, String time, String bid, String ask) {
    }

    private record CandlestickFileLineModel(String openDate, String openTime, String open, String high, String low, String close) {
    }
}
