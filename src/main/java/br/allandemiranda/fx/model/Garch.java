package br.allandemiranda.fx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
public class Garch {

    @Id
    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private LocalDateTime timestamp;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double lastSigmaPips;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double kTakeProfit;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double kStopLoss;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double var975;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double var990;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Garch garch = (Garch) o;
        return getTimestamp() != null && Objects.equals(getTimestamp(), garch.getTimestamp());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
