package br.allandemiranda.fx.utils;

import br.allandemiranda.fx.enums.Timeframe;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@UtilityClass
public final class TimeframeUtils {

    public static LocalDateTime getCandlestickTimestamp(LocalDateTime timestamp, @NotNull Timeframe timeFrame) {
        return switch (timeFrame.getFrame()) {
            case MINUTE -> getMinuteTime(timestamp, timeFrame);
            case HOUR -> getHourTime(timestamp, timeFrame);
            case DAY -> getDayTime(timestamp, timeFrame);
        };
    }

    private static @NotNull LocalDateTime getDayTime(@NotNull LocalDateTime timestamp, @NotNull Timeframe timeFrame) {
        final LocalTime localTime = LocalTime.of(0, 0, 0);
        if (timestamp.getDayOfMonth() % timeFrame.getTimeValue() == 0) {
            return LocalDateTime.of(LocalDate.of(timestamp.getYear(), timestamp.getMonth(), timestamp.getDayOfMonth() - (timeFrame.getTimeValue() - 1)), localTime);
        } else {
            final int div = timestamp.getDayOfMonth() / timeFrame.getTimeValue();
            final int newDay = div * timeFrame.getTimeValue();
            return LocalDateTime.of(LocalDate.of(timestamp.getYear(), timestamp.getMonth(), newDay + 1), localTime);
        }
    }

    private static @NotNull LocalDateTime getHourTime(@NotNull LocalDateTime timestamp, @NotNull Timeframe timeFrame) {
        final int div = timestamp.getHour() / timeFrame.getTimeValue();
        final int newHour = div * timeFrame.getTimeValue();
        final LocalTime candlestickTime = LocalTime.of(newHour, 0, 0);
        return LocalDateTime.of(timestamp.toLocalDate(), candlestickTime);
    }

    private static @NotNull LocalDateTime getMinuteTime(@NotNull LocalDateTime timestamp, @NotNull Timeframe timeFrame) {
        final int div = timestamp.getMinute() / timeFrame.getTimeValue();
        final int newMinute = div * timeFrame.getTimeValue();
        final LocalTime candlestickTime = LocalTime.of(timestamp.getHour(), newMinute, 0);
        return LocalDateTime.of(timestamp.toLocalDate(), candlestickTime);
    }
}
