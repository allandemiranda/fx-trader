package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.GarchTradingDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface GarchTradingService {
    @NotNull
    GarchTradingDto addGarchTrading(@NotNull @Valid GarchTradingDto garchTradingDto);
}
