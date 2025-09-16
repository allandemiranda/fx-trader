package br.allandemiranda.fx.utils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Collection;

@UtilityClass
public final class MovingAverageUtils {

    public static @NotNull BigDecimal getSimpleMovingAverage(@NotNull Collection<BigDecimal> prices, @Positive int period) {
        if (period != prices.size()) throw new IllegalArgumentException("period must be equal to prices size");
        BigDecimal divisor = BigDecimal.valueOf(period);
        return prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(divisor, MathUtils.PRECISION);
    }

    public static @NotNull BigDecimal getExponentialMovingAverage(@NotNull BigDecimal prevEma, @NotNull BigDecimal currentPrice, @Positive int period) {
        BigDecimal alpha = alpha(period);
        // EMA_t = EMA_{t-1} + α*(close - EMA_{t-1})
        return prevEma.add(alpha.multiply(currentPrice.subtract(prevEma, MathUtils.PRECISION), MathUtils.PRECISION), MathUtils.PRECISION);
    }

    private static @NotNull BigDecimal alpha(@Positive int period) {
        return new BigDecimal(2).divide(BigDecimal.valueOf(period + 1L), MathUtils.PRECISION);
    }
}
