package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.RSI;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RsiRepository extends Repository<RSI, LocalDateTime> {

    RSI save(RSI rsi);

    Optional<RSI> getRSIByTimestamp(LocalDateTime timestamp);
}