package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.EMAs;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.LocalDateTime;

public interface EmasRepository extends Repository<EMAs, LocalDateTime> {

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE EMAS", nativeQuery = true)
    void truncate();

    EMAs save(@Valid EMAs emas);
}