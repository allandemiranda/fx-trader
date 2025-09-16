package br.allandemiranda.fx.enums;

import br.allandemiranda.fx.dto.CandlestickDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

public enum AppliedPrice {
    //@formatter:off
    PRICE_CLOSE,   // Close price
    PRICE_OPEN,    // Open price
    PRICE_HIGH,    // The maximum price for the period
    PRICE_LOW,     // The minimum price for the period
    PRICE_MEDIAN,  // Median price, (high + low)/2
    PRICE_TYPICAL, // Typical price, (high + low + close)/3
    PRICE_WEIGHTED;// Average price, (high + low + close + close)/4
    //@formatter:on

    private static final int SCALE_MEDIAN = 6;
    private static final int SCALE_TYPICAL = 7;
    private static final int SCALE_WEIGHTED = 7;

    private double getPrice(Function<AppliedPrice, BigDecimal> valueExtractor) {
        BigDecimal result = switch (this) {
            case PRICE_CLOSE -> valueExtractor.apply(PRICE_CLOSE);
            case PRICE_OPEN -> valueExtractor.apply(PRICE_OPEN);
            case PRICE_HIGH -> valueExtractor.apply(PRICE_HIGH);
            case PRICE_LOW -> valueExtractor.apply(PRICE_LOW);
            case PRICE_MEDIAN -> valueExtractor.apply(PRICE_HIGH)
                    .add(valueExtractor.apply(PRICE_LOW))
                    .divide(BigDecimal.valueOf(2), SCALE_MEDIAN, RoundingMode.HALF_UP);
            case PRICE_TYPICAL -> valueExtractor.apply(PRICE_HIGH)
                    .add(valueExtractor.apply(PRICE_LOW))
                    .add(valueExtractor.apply(PRICE_CLOSE))
                    .divide(BigDecimal.valueOf(3), SCALE_TYPICAL, RoundingMode.HALF_UP);
            case PRICE_WEIGHTED -> valueExtractor.apply(PRICE_HIGH)
                    .add(valueExtractor.apply(PRICE_LOW))
                    .add(valueExtractor.apply(PRICE_CLOSE).multiply(BigDecimal.valueOf(2)))
                    .divide(BigDecimal.valueOf(4), SCALE_WEIGHTED, RoundingMode.HALF_UP);
        };
        return result.doubleValue();
    }

    public double getPrice(CandlestickDto candlestick) {
        return this.getPrice(priceType -> switch (priceType) {
            case PRICE_CLOSE -> BigDecimal.valueOf(candlestick.getClose());
            case PRICE_OPEN -> BigDecimal.valueOf(candlestick.getOpen());
            case PRICE_HIGH -> BigDecimal.valueOf(candlestick.getHigh());
            case PRICE_LOW -> BigDecimal.valueOf(candlestick.getLow());
            default -> throw new IllegalStateException("Unexpected value: " + priceType);
        });
    }
}
