package br.allandemiranda.fx.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
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
public class TickDto implements Serializable {

    @NotNull
    @PastOrPresent
    LocalDateTime timestamp;

    @Positive
    double bid;

    @Positive
    double ask;

    @AssertTrue(message = "bid price needs be equals or less than ask")
    public boolean isPriceValid() {
        return this.getBid() <= this.getAsk();
    }

}