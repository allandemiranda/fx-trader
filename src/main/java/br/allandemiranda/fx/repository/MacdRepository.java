package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.MACD;
import jakarta.validation.Valid;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MacdRepository extends Repository<MACD, LocalDateTime> {

    MACD save(MACD macd);

    Optional<MACD> getMACDByTimestamp(LocalDateTime timestamp);
}