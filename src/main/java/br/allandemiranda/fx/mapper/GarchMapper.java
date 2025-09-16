package br.allandemiranda.fx.mapper;

import br.allandemiranda.fx.dto.GarchDto;
import br.allandemiranda.fx.model.Garch;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public final class GarchMapper {

    public static @NotNull Garch toEntity(@NotNull GarchDto garchDto) {
        Garch garch = new Garch();
        garch.setVar990(garchDto.var990());
        garch.setVar975(garchDto.var975());
        garch.setKStopLoss(garchDto.kStopLoss());
        garch.setKTakeProfit(garchDto.kTakeProfit());
        garch.setLastSigmaPips(garchDto.lastSigmaPips());
        return garch;
    }

    public static @NotNull GarchDto toDto(@NotNull Garch entity) {
        return new GarchDto(entity.getTimestamp(), entity.getLastSigmaPips(), entity.getKTakeProfit(), entity.getKStopLoss(), entity.getVar990(), entity.getVar975());
    }
}
