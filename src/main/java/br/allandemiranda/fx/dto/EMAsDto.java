package br.allandemiranda.fx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
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
public class EMAsDto implements Serializable {

    @NotNull
    @PastOrPresent
    LocalDateTime timestamp;

    @NotNull
    @Positive
    BigDecimal emaHigh;

    @NotNull
    @Positive
    BigDecimal emaMid;

    @NotNull
    @Positive
    BigDecimal emaLow;
}