package br.allandemiranda.fx.utils;

import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@UtilityClass
public final class ReadFileDataUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").optionalStart().appendPattern(".SSS").optionalEnd().toFormatter();

    public static @NotNull LocalDateTime getDateTime(String date, String time) {
        LocalDate localDate = LocalDate.parse(date, ReadFileDataUtils.DATE_FORMATTER);
        LocalTime localTime = LocalTime.parse(time, ReadFileDataUtils.TIME_FORMATTER);
        return LocalDateTime.of(localDate, localTime);
    }

    public static double getPrice(String price) {
        try {
            return Double.parseDouble(price);
        } catch (Exception e) {
            return -1D;
        }
    }
}
