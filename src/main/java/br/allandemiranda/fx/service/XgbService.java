package br.allandemiranda.fx.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.Optional;

public interface XgbService {
    @SneakyThrows
    void runTrainer(float[][] x, float[] y);

    @SneakyThrows
    void saveTrainer(@Valid @NotNull Path location);

    @SneakyThrows
    void loadTrainer(@Valid @NotNull Path location);

    Optional<float[][]> getPredicate(float[] x);
}
