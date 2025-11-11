package br.allandemiranda.fx.utils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Collection;

@UtilityClass
public final class MovingAverageUtils {

    public static @NotNull BigDecimal getSimpleMovingAverage(@NotNull Collection<BigDecimal> prices) {
        if(prices.isEmpty()) {
            throw new IllegalArgumentException("Prices collection is empty");
        } else {
            final BigDecimal period = BigDecimal.valueOf(prices.size());
            final BigDecimal sum = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            return BigDecimal.ZERO.equals(sum) ? sum : sum.divide(period, MathUtils.PRECISION);
        }
    }

    public static @NotNull BigDecimal getExponentialMovingAverage(@NotNull BigDecimal prevEma, @NotNull BigDecimal currentPrice, @Positive int period) {
        final BigDecimal alpha = alpha(period);
        // EMA_t = EMA_{t-1} + α*(close - EMA_{t-1})
        return prevEma.add(alpha.multiply(currentPrice.subtract(prevEma, MathUtils.PRECISION), MathUtils.PRECISION), MathUtils.PRECISION);
    }

    private static @NotNull BigDecimal alpha(@Positive int period) {
        return new BigDecimal(2).divide(BigDecimal.valueOf(period + 1L), MathUtils.PRECISION);
    }
}
