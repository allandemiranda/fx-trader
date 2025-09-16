package br.allandemiranda.fx.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
@Table
@Entity
public class EMAs {

    @Id
    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @PastOrPresent
    private LocalDateTime timestamp;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DECIMAL)
    @Positive
    private BigDecimal emaHigh;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DECIMAL)
    @Positive
    private BigDecimal emaMid;

    @NotNull
    @Column(nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.DECIMAL)
    @Positive
    private BigDecimal emaLow;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        EMAs emAs = (EMAs) o;
        return getTimestamp() != null && Objects.equals(getTimestamp(), emAs.getTimestamp());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
