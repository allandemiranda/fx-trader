package br.allandemiranda.fx.mapper;

import br.allandemiranda.fx.dto.CandlestickDto;
import br.allandemiranda.fx.dto.GarchDto;
import br.allandemiranda.fx.model.Candlestick;
import br.allandemiranda.fx.model.Garch;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class CandlestickMapper {

    public static @NotNull Candlestick toEntity(@NotNull CandlestickDto dto) {
        Candlestick candlestick = new Candlestick();
        candlestick.setOpenDateTime(dto.openDateTime());
        candlestick.setOpen(dto.open());
        candlestick.setHigh(dto.high());
        candlestick.setLow(dto.low());
        candlestick.setClose(dto.close());
        Garch garch = dto.garch() != null ? GarchMapper.toEntity(dto.garch()) : null;
        candlestick.setGarch(garch);
        return candlestick;
    }

    public static @NotNull CandlestickDto toDto(@NotNull Candlestick entity) {
        GarchDto garchDto = entity.getGarch() != null ? GarchMapper.toDto(entity.getGarch()) : null;
        return new CandlestickDto(entity.getOpenDateTime(), entity.getOpen(), entity.getHigh(), entity.getLow(), entity.getClose(), garchDto);
    }
}
