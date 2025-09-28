package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.MACDDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MacdService {
    @NotNull
    MACDDto addMACD(@NotNull @Valid MACDDto macdDto);

    Optional<MACDDto> getMACD(@NotNull @Valid @PastOrPresent LocalDateTime timestamp);
}
