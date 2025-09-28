package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.Garch;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GarchRepository extends Repository<Garch, LocalDateTime> {

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE GARCH", nativeQuery = true)
    void truncate();

    Garch save(Garch garch);

    Optional<Garch> getGarchByTimestamp(LocalDateTime timestamp);
}