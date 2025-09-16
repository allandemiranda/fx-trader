package br.allandemiranda.fx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class Tick {

    @Id
    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private LocalDateTime dateTime;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Positive
    private BigDecimal bid;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Positive
    private BigDecimal ask;

    @AssertTrue(message = "bid price needs be equals or less than ask")
    public boolean isPriceValid() {
        return this.getBid().compareTo(this.getAsk()) <= 0;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Tick tick = (Tick) o;
        return getDateTime() != null && Objects.equals(getDateTime(), tick.getDateTime());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
