package br.allandemiranda.fx.model.embeddable;

import br.allandemiranda.fx.enums.DealReason;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Data
@Embeddable
public class GarchPositionType {

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @PastOrPresent
    private LocalDateTime closeTime;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    private double profit;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double swapCost = 0D;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private DealReason dealReason = DealReason.NONE;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double openPrice;
}
