package br.allandemiranda.fx.utils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

@UtilityClass
public final class SwapUtils {

    private static final Set<DayOfWeek> ROLLOVER_DAYS = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    public static int countRolloverUnits(@NotNull LocalDateTime open, @NotNull LocalDateTime close, @NotNull DayOfWeek tripleDay) {
        if (!close.isAfter(open)) return 0;

        final LocalDate start = open.toLocalDate().plusDays(1);
        LocalDate end = close.toLocalDate();

        if (close.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            end = end.minusDays(1);
        }

        int units = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            final DayOfWeek dow = d.getDayOfWeek();
            if (!ROLLOVER_DAYS.contains(dow)) continue;
            units += (dow == tripleDay) ? 3 : 1;
        }
        return Math.max(units, 0);
    }

    public static double calcSwapInPoints(boolean isBuy, double swapLongPts, double swapShortPts, @PositiveOrZero int rolloverUnits) {
        final double nightlyPts = isBuy ? swapLongPts : swapShortPts;
        return nightlyPts * rolloverUnits;
    }
}
