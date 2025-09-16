package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.RSI;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;

public interface RsiRepository extends Repository<RSI, LocalDateTime> {

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE RSI", nativeQuery = true)
    void truncate();

    RSI save(RSI rsi);
}