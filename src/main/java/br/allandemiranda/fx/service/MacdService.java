package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.MACDDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface MacdService {
    @NotNull
    MACDDto addMACD(@NotNull @Valid MACDDto macdDto);
}
