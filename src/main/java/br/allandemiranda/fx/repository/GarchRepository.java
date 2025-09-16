package br.allandemiranda.fx.repository;

import br.allandemiranda.fx.model.Candlestick;
import br.allandemiranda.fx.model.Garch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GarchRepository extends JpaRepository<Garch, Candlestick> {
}