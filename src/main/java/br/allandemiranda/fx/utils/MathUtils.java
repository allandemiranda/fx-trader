package br.allandemiranda.fx.utils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class MathUtils {

    public static final MathContext PRECISION = new MathContext(12, RoundingMode.HALF_UP);

    public static @NotNull BigDecimal getDecimal(double value) {
        return BigDecimal.valueOf(value);
    }

    public static <T> @NotNull List<T> lastN(@NotNull List<T> list, @Positive int n) {
        if (n >= list.size()) return List.copyOf(list);
        return new ArrayList<>(list.subList(list.size() - n, list.size()));
    }

    public static @NotNull String formatDuration(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        if (duration.isNegative()) {
            duration = duration.negated();
        }

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }
}
