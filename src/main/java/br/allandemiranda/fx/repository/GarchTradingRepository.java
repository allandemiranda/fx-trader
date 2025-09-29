package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.GarchTrading;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GarchTradingRepository extends Repository<GarchTrading, LocalDateTime> {

    GarchTrading save(GarchTrading garchTrading);

    Optional<GarchTrading> getGarchByTimestamp(LocalDateTime timestamp);
}