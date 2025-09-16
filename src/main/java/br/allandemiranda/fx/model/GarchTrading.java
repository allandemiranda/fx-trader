package br.allandemiranda.fx.model;

import br.allandemiranda.fx.model.embeddable.GarchPositionType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table
@Entity
public class GarchTrading {

    @Id
    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @PastOrPresent
    private LocalDateTime timestamp;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @PastOrPresent
    private LocalDateTime openTime;

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "closeTime",  column = @Column(name = "buy_close_time",  nullable = false, updatable = false)),
            @AttributeOverride(name = "profit",     column = @Column(name = "buy_profit",      nullable = false, updatable = false)),
            @AttributeOverride(name = "swapCost",   column = @Column(name = "buy_swap_cost",   nullable = false, updatable = false)),
            @AttributeOverride(name = "dealReason", column = @Column(name = "buy_deal_reason", nullable = false, updatable = false)),
            @AttributeOverride(name = "openPrice",  column = @Column(name = "buy_open_price",  nullable = false, updatable = false))
    })
    private GarchPositionType positionTypeBuy;

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "closeTime",  column = @Column(name = "sell_close_time",  nullable = false, updatable = false)),
            @AttributeOverride(name = "profit",     column = @Column(name = "sell_profit",      nullable = false, updatable = false)),
            @AttributeOverride(name = "swapCost",   column = @Column(name = "sell_swap_cost",   nullable = false, updatable = false)),
            @AttributeOverride(name = "dealReason", column = @Column(name = "sell_deal_reason", nullable = false, updatable = false)),
            @AttributeOverride(name = "openPrice",  column = @Column(name = "sell_open_price",  nullable = false, updatable = false))
    })
    private GarchPositionType positionTypeSell;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double spread;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        GarchTrading that = (GarchTrading) o;
        return getTimestamp() != null && Objects.equals(getTimestamp(), that.getTimestamp());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
