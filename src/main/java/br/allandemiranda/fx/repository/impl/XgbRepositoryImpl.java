package br.allandemiranda.fx.repository.impl;

import br.allandemiranda.fx.repository.XgbRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ml.dmlc.xgboost4j.java.Booster;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Repository
public class XgbRepositoryImpl implements XgbRepository {

    private Booster booster;

    @Transactional(readOnly = true)
    @Override
    public Optional<Booster> getXgb() {
        return Optional.ofNullable(this.getBooster());
    }

    @Modifying
    @Override
    public void save(Booster booster) {
        this.setBooster(booster);
    }
}
