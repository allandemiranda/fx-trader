package br.allandemiranda.fx.share;

import br.allandemiranda.fx.utils.MathUtils;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@UtilityClass
public final class XgbTrainer {

    /**
     * Treina em 100% dos dados
     *
     * @param X         matriz de features [n][p] (row-major)
     * @param y         rótulos [n] (0..numClasses-1)
     * @param numRounds número de boosting rounds (ex.: 300)
     * @return Booster treinado (você pode reutilizar para prever em memória)
     */
    public static Booster trainAll(Map<String, Object> params, float[][] X, float[] y, int numRounds) throws XGBoostError {
        if (X == null || X.length == 0) throw new IllegalArgumentException("X empty.");
        int n = X.length, p = X[0].length;
        if (y == null || y.length != n) throw new IllegalArgumentException("y incompatible with X.");

        DMatrix dtrain = XgbTrainer.toDMatrix(X, y);

        // Treino (watch apenas no train, já que não há validação)
        Map<String, DMatrix> watch = Map.of("train", dtrain);
        log.info("Starting training...");
        LocalDateTime start = LocalDateTime.now();
        Booster booster = XGBoost.train(dtrain, params, numRounds, watch, null, null);

        // Salvar
        //booster.saveModel(outModel.toString());

        log.info("Training finished in {}.", MathUtils.formatDuration(start, LocalDateTime.now()));

        return booster;
    }

    // * @param numClasses número de classes (ex.: 3 para BUY/SELL/NEUTRAL)
    //     * @param useGpu     true para usar GPU (tree_method=gpu_hist)
    //     * @param gpuId      id da GPU CUDA (geralmente 0 se há 1 GPU NVIDIA)
    public static @NotNull Map<String, Object> defaultParams(int numClasses, boolean useGpu, int gpuId, Integer seed) {
        Map<String, Object> p = new HashMap<>();
        p.put("objective", "multi:softprob");
        p.put("num_class", numClasses);
        p.put("eval_metric", "mlogloss");

        // Hiperparâmetros comuns (ponto de partida)
        p.put("max_depth", 4);
        p.put("eta", 0.2);              // learning rate
        p.put("subsample", 1.0);
        p.put("colsample_bytree", 1.0);
        p.put("min_child_weight", 10);
        p.put("lambda", 5.0);            // L2
        p.put("alpha", 0.0);             // L1
        if (seed != null) p.put("seed", seed);

        // CPU vs GPU
        if (useGpu) {
            p.put("tree_method", "gpu_hist");
            p.put("predictor", "gpu_predictor");
            p.put("gpu_id", gpuId);
        } else {
            p.put("tree_method", "hist");
            p.put("nthread", Runtime.getRuntime().availableProcessors());
        }

        return p;
    }

    private static @NotNull DMatrix toDMatrix(float[][] X, float[] y) throws XGBoostError {
        int n = X.length, p = X[0].length;
        float[] data = new float[n * p];
        int idx = 0;
        for (float[] x : X) {
            for (int j = 0; j < p; j++) {
                data[idx++] = x[j];
            }
        }
        DMatrix d = new DMatrix(data, n, p, Float.NaN);
        d.setLabel(y);
        return d;
    }

    @SneakyThrows
    public static void saveModel(@NotNull Booster booster, @NotNull Path location) {
        booster.saveModel(location.toString());
    }

    @SneakyThrows
    public static float[][] runPredicateSimple(float[] x, Booster booster) {
        // Cria DMatrix com 1 linha e p colunas
        DMatrix d = new DMatrix(x, 1, x.length, Float.NaN);

        // Probabilidades por classe (multi:softprob)
        return booster.predict(d); // shape: [1][num_class]
    }

}
