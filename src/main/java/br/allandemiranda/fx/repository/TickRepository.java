package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.Tick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TickRepository extends JpaRepository<Tick, LocalDateTime> {

    Optional<Tick> findFirstByOrderByDateTimeAsc();
}