package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.EMAs;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmasRepository extends Repository<EMAs, LocalDateTime> {

    EMAs save(EMAs emas);

    Optional<EMAs> getEmasByTimestamp(LocalDateTime timestamp);
}