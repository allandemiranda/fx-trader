package br.allandemiranda.fx.service;

import br.allandemiranda.fx.dto.TickDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface TickService {
    @SneakyThrows
    void consumerTicks(@Valid @NotNull Path tickFile, @Valid @NotNull Consumer<TickDto> consumer);
}
