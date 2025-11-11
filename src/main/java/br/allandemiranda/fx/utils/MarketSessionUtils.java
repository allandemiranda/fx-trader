package br.allandemiranda.fx.utils;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.EnumSet;

@UtilityClass
public final class MarketSessionUtils {
    private static final LocalTime SYDNEY_OPEN = LocalTime.of(8, 0);
    private static final LocalTime SYDNEY_CLOSE = LocalTime.of(17, 0);
    private static final LocalTime TOKYO_OPEN = LocalTime.of(9, 0);
    private static final LocalTime TOKYO_CLOSE = LocalTime.of(18, 0);
    private static final LocalTime LONDON_OPEN = LocalTime.of(8, 0);
    private static final LocalTime LONDON_CLOSE = LocalTime.of(17, 0);
    private static final LocalTime NY_OPEN = LocalTime.of(8, 0);
    private static final LocalTime NY_CLOSE = LocalTime.of(17, 0);

    public static Session detectSession(@NotNull ZonedDateTime dateTime) {
        final Instant instant = dateTime.toInstant();
        final EnumSet<Market> active = EnumSet.noneOf(Market.class);
        if (MarketSessionUtils.isSydneyOpen(instant)) active.add(Market.SYDNEY);
        if (MarketSessionUtils.isTokyoOpen(instant)) active.add(Market.TOKYO);
        if (MarketSessionUtils.isLondonOpen(instant)) active.add(Market.LONDON);
        if (MarketSessionUtils.isNewYorkOpen(instant)) active.add(Market.NEW_YORK);

        return MarketSessionUtils.toMarketSession(active);
    }

    public static boolean isBusinessDay(@NotNull ZonedDateTime zdt) {
        final DayOfWeek dow = zdt.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    private static boolean within(@NotNull LocalTime t, @NotNull LocalTime open, @NotNull LocalTime close) {
        return !t.isBefore(open) && t.isBefore(close);
    }

    private static boolean isSydneyOpen(@NotNull Instant instant) {
        final ZonedDateTime z = instant.atZone(ZoneUtils.ZONE_SYDNEY);
        return MarketSessionUtils.isBusinessDay(z) && MarketSessionUtils.within(z.toLocalTime(), SYDNEY_OPEN, SYDNEY_CLOSE);
    }

    private static boolean isTokyoOpen(@NotNull Instant instant) {
        final ZonedDateTime z = instant.atZone(ZoneUtils.ZONE_TOKYO);
        return MarketSessionUtils.isBusinessDay(z) && MarketSessionUtils.within(z.toLocalTime(), TOKYO_OPEN, TOKYO_CLOSE);
    }

    private static boolean isLondonOpen(@NotNull Instant instant) {
        final ZonedDateTime z = instant.atZone(ZoneUtils.ZONE_LONDON);
        return MarketSessionUtils.isBusinessDay(z) && MarketSessionUtils.within(z.toLocalTime(), LONDON_OPEN, LONDON_CLOSE);
    }

    private static boolean isNewYorkOpen(@NotNull Instant instant) {
        final ZonedDateTime z = instant.atZone(ZoneUtils.ZONE_NEW_YORK);
        return MarketSessionUtils.isBusinessDay(z) && MarketSessionUtils.within(z.toLocalTime(), NY_OPEN, NY_CLOSE);
    }

    private static Session toMarketSession(@NotNull EnumSet<Market> set) {
        final String key = set.stream().sorted().toList().toString();

        return switch (key) {
            case "[SYDNEY]" -> Session.SYDNEY;
            case "[TOKYO]" -> Session.TOKYO;
            case "[LONDON]" -> Session.LONDON;
            case "[NEW_YORK]" -> Session.NEW_YORK;

            case "[SYDNEY, TOKYO]" -> Session.SYDNEY_TOKYO;
            case "[TOKYO, LONDON]" -> Session.TOKYO_LONDON;
            case "[LONDON, NEW_YORK]" -> Session.LONDON_NEW_YORK;

            default -> Session.NONE;
        };
    }

    @Getter
    @AllArgsConstructor
    public enum Session {
        NONE(0),
        SYDNEY(1),
        SYDNEY_TOKYO(2),
        TOKYO(3),
        TOKYO_LONDON(4),
        LONDON(5),
        LONDON_NEW_YORK(6),
        NEW_YORK(7);

        private final int value;
    }

    private enum Market {SYDNEY, TOKYO, LONDON, NEW_YORK}
}

