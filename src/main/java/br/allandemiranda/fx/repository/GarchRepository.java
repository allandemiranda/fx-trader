package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.Garch;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GarchRepository extends Repository<Garch, LocalDateTime> {

    Garch save(Garch garch);

    Optional<Garch> getGarchByTimestamp(LocalDateTime timestamp);
}