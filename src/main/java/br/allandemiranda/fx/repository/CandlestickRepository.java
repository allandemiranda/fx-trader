package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.Candlestick;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.stream.Stream;

public interface CandlestickRepository extends JpaRepository<Candlestick, LocalDateTime> {

    @Query(
            value = """
                    SELECT * FROM (
                        SELECT *
                        FROM candlestick c
                        WHERE c.id <= :pivot
                        ORDER BY c.id DESC
                        LIMIT :n
                    ) sub
                    ORDER BY sub.id ASC
                    """,
            nativeQuery = true
    )
    @QueryHints({
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    Stream<Candlestick> streamLastNBeforeOrEqualAsc(@Param("pivot") LocalDateTime pivot, @Param("n") long n);
}