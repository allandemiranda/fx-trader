package br.allandemiranda.fx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class RSIDto implements Serializable {

    @NotNull
    @PastOrPresent
    LocalDateTime timestamp;

    @NotNull
    @PositiveOrZero
    BigDecimal rsi;
}