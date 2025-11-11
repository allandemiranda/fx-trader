package br.allandemiranda.fx.utils;

import br.allandemiranda.fx.enums.Timeframe;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@UtilityClass
public final class TimeframeUtils {

    public static ZonedDateTime getCandlestickTimestamp(@NotNull ZonedDateTime timestamp, @NotNull Timeframe timeFrame) {
        return switch (timeFrame.getFrame()) {
            case MINUTE -> TimeframeUtils.getMinuteTime(timestamp, timeFrame);
            case HOUR -> TimeframeUtils.getHourTime(timestamp, timeFrame);
            case DAY -> TimeframeUtils.getDayTime(timestamp, timeFrame);
        };
    }

    private static @NotNull ZonedDateTime getDayTime(@NotNull ZonedDateTime timestamp, @NotNull Timeframe timeFrame) {
        final LocalTime localTime = LocalTime.of(0, 0, 0);
        final ZoneId zone = timestamp.getZone();
        if (timestamp.getDayOfMonth() % timeFrame.getTimeValue() == 0) {
            return ZonedDateTime.of(LocalDate.of(timestamp.getYear(), timestamp.getMonth(), timestamp.getDayOfMonth() - (timeFrame.getTimeValue() - 1)), localTime, zone);
        } else {
            final int div = timestamp.getDayOfMonth() / timeFrame.getTimeValue();
            final int newDay = div * timeFrame.getTimeValue();
            return ZonedDateTime.of(LocalDate.of(timestamp.getYear(), timestamp.getMonth(), newDay + 1), localTime, zone);
        }
    }

    private static @NotNull ZonedDateTime getHourTime(@NotNull ZonedDateTime timestamp, @NotNull Timeframe timeFrame) {
        final int div = timestamp.getHour() / timeFrame.getTimeValue();
        final int newHour = div * timeFrame.getTimeValue();
        final LocalTime candlestickTime = LocalTime.of(newHour, 0, 0);
        return ZonedDateTime.of(timestamp.toLocalDate(), candlestickTime, timestamp.getZone());
    }

    private static @NotNull ZonedDateTime getMinuteTime(@NotNull ZonedDateTime timestamp, @NotNull Timeframe timeFrame) {
        final int div = timestamp.getMinute() / timeFrame.getTimeValue();
        final int newMinute = div * timeFrame.getTimeValue();
        final LocalTime candlestickTime = LocalTime.of(timestamp.getHour(), newMinute, 0);
        return ZonedDateTime.of(timestamp.toLocalDate(), candlestickTime, timestamp.getZone());
    }
}
