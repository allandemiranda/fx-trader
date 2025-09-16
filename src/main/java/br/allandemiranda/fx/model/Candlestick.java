package br.allandemiranda.fx.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table
public class Candlestick {

    @Id
    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private LocalDateTime openDateTime;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Positive
    private BigDecimal open;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Positive
    private BigDecimal high;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Positive
    private BigDecimal low;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Positive
    private BigDecimal close;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "garch_timestamp", unique = true)
    private Garch garch;

    @AssertTrue(message = "low must be <= open/close and high must be >= open/close")
    public boolean isPriceValid() {
        return this.getLow().compareTo(this.getHigh()) <= 0 &&
                this.getOpen().compareTo(this.getHigh()) <= 0 &&
                this.getOpen().compareTo(this.getLow()) >= 0 &&
                this.getClose().compareTo(this.getHigh()) <= 0 &&
                this.getClose().compareTo(this.getLow()) >= 0;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Candlestick that = (Candlestick) o;
        return getOpenDateTime() != null && Objects.equals(getOpenDateTime(), that.getOpenDateTime());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
