package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.GarchTrading;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;

public interface GarchTradingRepository extends Repository<GarchTrading, LocalDateTime> {

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE GARCH_TRADING", nativeQuery = true)
    void truncate();

    GarchTrading save(@Valid GarchTrading garchTrading);
}