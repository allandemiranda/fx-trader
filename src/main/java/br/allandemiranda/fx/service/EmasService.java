package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.EMAsDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmasService {
    @NotNull
    EMAsDto add(@NotNull @Valid EMAsDto emasDto);

    @NotNull
    Optional<EMAsDto> getEmas(@NotNull @Valid LocalDateTime timestamp);
}
