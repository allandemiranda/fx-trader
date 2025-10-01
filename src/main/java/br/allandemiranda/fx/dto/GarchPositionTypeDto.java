package br.allandemiranda.fx.dto;

import br.allandemiranda.fx.enums.DealReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class GarchPositionTypeDto implements Serializable {
    @NotNull
    @PastOrPresent
    LocalDateTime closeTime;

    double profit;

    double swapCost;

    @NotNull
    DealReason dealReason;

    @PositiveOrZero
    double openPrice;
}