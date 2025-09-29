package br.allandemiranda.fx.service.impl;

import br.allandemiranda.fx.repository.XgbRepository;
import br.allandemiranda.fx.service.XgbService;
import br.allandemiranda.fx.share.XgbTrainer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Getter(AccessLevel.PRIVATE)
@Transactional
@Service
public class XgbServiceImpl implements XgbService {

    private final XgbRepository xgbRepository;

    @SneakyThrows
    @Override
    public void runTrainer(float[][] x, float[] y) {
        Booster booster = XgbTrainer.trainAll(XgbTrainer.defaultParams(3, true, 0, null), x, y, 300);
        this.getXgbRepository().save(booster);
    }

    @SneakyThrows
    @Override
    public void saveTrainer(@Valid @NotNull Path location) {
        this.getXgbRepository().getXgb().ifPresent(booster -> XgbTrainer.saveModel(booster, location));
    }

    @SneakyThrows
    @Override
    public void loadTrainer(@Valid @NotNull Path location) {
        Booster booster = XGBoost.loadModel(location.toString());
        this.getXgbRepository().save(booster);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<float[][]> getPredicate(float[] x) {
        return this.getXgbRepository().getXgb().map(booster -> XgbTrainer.runPredicateSimple(x, booster));
    }
}
