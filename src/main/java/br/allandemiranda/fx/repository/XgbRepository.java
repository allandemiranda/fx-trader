package br.allandemiranda.fx.repository;

import ml.dmlc.xgboost4j.java.Booster;

import java.util.Optional;

public interface XgbRepository {

    Optional<Booster> getXgb();

    void save(Booster booster);
}
