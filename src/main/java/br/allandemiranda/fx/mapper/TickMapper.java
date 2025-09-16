package br.allandemiranda.fx.mapper;

import br.allandemiranda.fx.dto.TickDto;
import br.allandemiranda.fx.model.Tick;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class TickMapper {

    public static @NotNull Tick toEntity(@NotNull TickDto dto) {
        Tick tick = new Tick();
        tick.setDateTime(dto.dateTime());
        tick.setAsk(dto.ask());
        tick.setBid(dto.bid());
        return tick;
    }

    public static @NotNull TickDto toDto(@NotNull Tick tick) {
        return new TickDto(tick.getDateTime(), tick.getAsk(), tick.getBid());
    }
}
