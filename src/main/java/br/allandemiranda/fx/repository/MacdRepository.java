package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.MACD;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MacdRepository extends Repository<MACD, LocalDateTime> {

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE MACD", nativeQuery = true)
    void truncate();

    MACD save(@Valid MACD macd);

    Optional<MACD> getMACDByTimestamp(LocalDateTime timestamp);
}