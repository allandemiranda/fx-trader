package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.EMAsDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface EmasService {
    @NotNull
    EMAsDto add(@NotNull @Valid EMAsDto emasDto);
}
