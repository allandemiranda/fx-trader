package br.allandemiranda.fx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class Garch {

    @Id
    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @PastOrPresent
    private LocalDateTime timestamp;

    // Fundamental variables for the decision to open a trade

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double tpPips;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double slPips;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double sigmaPerBarPips;

    // Important variables to understand the regime

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    private double mu;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double omega;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double alpha;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double beta;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double nu;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @PositiveOrZero
    private double breakEvenHitRate;

    // Variables to determinate (if true) is not good to open

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BOOLEAN)
    private boolean isLowPersistence;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BOOLEAN)
    private boolean isBetaInEdge;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BOOLEAN)
    private boolean isNuInEdge;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BOOLEAN)
    private boolean isInvertedQuantis;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BOOLEAN)
    private boolean isTpOmegaOut;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BOOLEAN)
    private boolean isSlOmegaSorter;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BOOLEAN)
    private boolean isSlOmegaLonger;

    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BOOLEAN)
    private boolean isOmegaPerHLonger;

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
