package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.GarchTradingDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GarchTradingService {
    @NotNull
    GarchTradingDto addGarchTrading(@NotNull @Valid GarchTradingDto garchTradingDto);

    Optional<GarchTradingDto> getGarchTrading(@NotNull @Valid @PastOrPresent LocalDateTime timestamp);
}
