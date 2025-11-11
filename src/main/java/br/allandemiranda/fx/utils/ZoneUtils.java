package br.allandemiranda.fx.utils;

import lombok.experimental.UtilityClass;

import java.time.ZoneId;

@UtilityClass
public final class ZoneUtils {
    public static final ZoneId MT5_ZONE = ZoneId.of("Europe/Athens");
    public static final ZoneId LOCAL_ZONE = ZoneId.of("Europe/Paris");
    public static final ZoneId ZONE_SYDNEY = ZoneId.of("Australia/Sydney");
    public static final ZoneId ZONE_TOKYO = ZoneId.of("Asia/Tokyo");
    public static final ZoneId ZONE_LONDON = ZoneId.of("Europe/London");
    public static final ZoneId ZONE_NEW_YORK = ZoneId.of("America/New_York");
}
