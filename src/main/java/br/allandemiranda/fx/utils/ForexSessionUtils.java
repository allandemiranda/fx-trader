package br.allandemiranda.fx.utils;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumSet;

@UtilityClass
public final class ForexSessionUtils {
    private static final ZoneId MT5_SERVER_ZONE = ZoneId.of("Europe/Athens");
    private static final ZoneId ZONE_SYDNEY = ZoneId.of("Australia/Sydney");
    private static final ZoneId ZONE_TOKYO = ZoneId.of("Asia/Tokyo");
    private static final ZoneId ZONE_LONDON = ZoneId.of("Europe/London");
    private static final ZoneId ZONE_NEW_YORK = ZoneId.of("America/New_York");
    private static final LocalTime SYDNEY_OPEN = LocalTime.of(8, 0);
    private static final LocalTime SYDNEY_CLOSE = LocalTime.of(17, 0);
    private static final LocalTime TOKYO_OPEN = LocalTime.of(9, 0);
    private static final LocalTime TOKYO_CLOSE = LocalTime.of(18, 0);
    private static final LocalTime LONDON_OPEN = LocalTime.of(8, 0);
    private static final LocalTime LONDON_CLOSE = LocalTime.of(17, 0);
    private static final LocalTime NY_OPEN = LocalTime.of(8, 0);
    private static final LocalTime NY_CLOSE = LocalTime.of(17, 0);

    public static MarketSession detectSession(@NotNull LocalDateTime mt5ServerLocalDateTime) {
        Instant instant = mt5ServerLocalDateTime.atZone(MT5_SERVER_ZONE).toInstant();

        EnumSet<Venue> active = EnumSet.noneOf(Venue.class);
        if (ForexSessionUtils.isSydneyOpen(instant)) active.add(Venue.SYDNEY);
        if (ForexSessionUtils.isTokyoOpen(instant)) active.add(Venue.TOKYO);
        if (ForexSessionUtils.isLondonOpen(instant)) active.add(Venue.LONDON);
        if (ForexSessionUtils.isNewYorkOpen(instant)) active.add(Venue.NEW_YORK);

        return ForexSessionUtils.toMarketSession(active);
    }

    private static boolean isBusinessDay(@NotNull ZonedDateTime zdt) {
        DayOfWeek dow = zdt.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    private static boolean within(@NotNull LocalTime t, @NotNull LocalTime open, @NotNull LocalTime close) {
        return !t.isBefore(open) && t.isBefore(close);
    }

    private static boolean isSydneyOpen(@NotNull Instant instant) {
        ZonedDateTime z = instant.atZone(ZONE_SYDNEY);
        return ForexSessionUtils.isBusinessDay(z) && ForexSessionUtils.within(z.toLocalTime(), SYDNEY_OPEN, SYDNEY_CLOSE);
    }

    private static boolean isTokyoOpen(@NotNull Instant instant) {
        ZonedDateTime z = instant.atZone(ZONE_TOKYO);
        return ForexSessionUtils.isBusinessDay(z) && ForexSessionUtils.within(z.toLocalTime(), TOKYO_OPEN, TOKYO_CLOSE);
    }

    private static boolean isLondonOpen(@NotNull Instant instant) {
        ZonedDateTime z = instant.atZone(ZONE_LONDON);
        return ForexSessionUtils.isBusinessDay(z) && ForexSessionUtils.within(z.toLocalTime(), LONDON_OPEN, LONDON_CLOSE);
    }

    private static boolean isNewYorkOpen(@NotNull Instant instant) {
        ZonedDateTime z = instant.atZone(ZONE_NEW_YORK);
        return ForexSessionUtils.isBusinessDay(z) && ForexSessionUtils.within(z.toLocalTime(), NY_OPEN, NY_CLOSE);
    }

    private static MarketSession toMarketSession(@NotNull EnumSet<Venue> set) {
        String key = set.stream().sorted().toList().toString();

        return switch (key) {
            case "[SYDNEY]" -> MarketSession.SESSION_SYDNEY;
            case "[TOKYO]" -> MarketSession.SESSION_TOKYO;
            case "[LONDON]" -> MarketSession.SESSION_LONDON;
            case "[NEW_YORK]" -> MarketSession.SESSION_NEW_YORK;

            case "[SYDNEY, TOKYO]" -> MarketSession.SESSION_SYDNEY_TOKYO;
            case "[TOKYO, LONDON]" -> MarketSession.SESSION_TOKYO_LONDON;
            case "[LONDON, NEW_YORK]" -> MarketSession.SESSION_LONDON_NEW_YORK;

            default -> MarketSession.SESSION_NONE;
        };
    }

    @Getter
    @AllArgsConstructor
    public enum MarketSession {
        SESSION_NONE(0),
        SESSION_SYDNEY(1),
        SESSION_SYDNEY_TOKYO(2),
        SESSION_TOKYO(3),
        SESSION_TOKYO_LONDON(4),
        SESSION_LONDON(5),
        SESSION_LONDON_NEW_YORK(6),
        SESSION_NEW_YORK(7);

        private final int value;
    }

    private enum Venue {SYDNEY, TOKYO, LONDON, NEW_YORK}
}

