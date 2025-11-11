package br.allandemiranda.fx.utils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@UtilityClass
public final class ReadFileDataUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").optionalStart().appendPattern(".SSS").optionalEnd().toFormatter();

    public static @NotNull ZonedDateTime getDateTime(@NotNull String date, @NotNull String time, @NotNull ZoneId zone) {
        final LocalDate localDate = LocalDate.parse(date, ReadFileDataUtils.DATE_FORMATTER);
        final LocalTime localTime = LocalTime.parse(time, ReadFileDataUtils.TIME_FORMATTER);
        return ZonedDateTime.of(localDate, localTime, zone);
    }

    public static @PositiveOrZero double getPrice(@NotNull String price) {
        return Double.parseDouble(price);
    }
}
