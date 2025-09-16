package br.allandemiranda.fx.service;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public interface ValidateBean {
    <T> void validate(@NotNull T bean, @NotNull LocalDateTime timestamp);

    <T> boolean isValidate(@NotNull T bean, @NotNull LocalDateTime timestamp);
}
