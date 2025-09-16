package br.allandemiranda.fx.share;

import br.allandemiranda.fx.dto.GarchDto;
import br.allandemiranda.fx.utils.GarchUtils;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GarchComputing {

    public Optional<GarchDto> computer(LocalDateTime timestamp, @NotNull List<Double> prices, double pipSize, int horizonBars, double alphaTP, double alphaSL) {
        // ---------------- Amostra ----------------
        final double[] px = prices.stream().mapToDouble(Double::doubleValue).toArray();
        final double pNow = px[px.length - 1];

        // Retornos log
        final int n = px.length;
        final double[] r = new double[n - 1];
        for (int i = 1; i < n; i++) r[i - 1] = Math.log(px[i] / px[i - 1]);

        // ---------------- Preparação MLE GARCH-t ----------------
        final double mu0Raw = GarchUtils.mean(r);
        final double var0 = GarchUtils.variance(r, mu0Raw);

        // piso de omega proporcional à variância (evitar degeneração em 1e-12)
        final double omegaFloor = Math.max(1e-10 * var0, 1e-11);

        // bounds + teto de persistência
        final double muCap = 2e-4;      // ~2 pips/h de drift máx (EURUSD H1)
        final double persistMax = 0.995;

        //             mu       omega       alpha   beta     nu
        final double[] lower = {-muCap, omegaFloor, 0.01, 0.80, 3.50};
        final double[] upper = {+muCap, 1.0, 0.35, 0.995, 40.0};

        // tentativa de chute via FINMATH GARCH-N
        Double omegaFin = null, alphaFin = null, betaFin = null;
        try {
            net.finmath.timeseries.models.parametric.GARCH gN = new net.finmath.timeseries.models.parametric.GARCH(px);
            Map<String, Object> bestN = gN.getBestParameters(null);
            omegaFin = ((Number) bestN.get("Omega")).doubleValue();
            alphaFin = ((Number) bestN.get("Alpha")).doubleValue();
            betaFin = ((Number) bestN.get("Beta")).doubleValue();
            // sanitiza
            if (alphaFin + betaFin >= persistMax) {
                double shrink = (persistMax - 1e-4) / (alphaFin + betaFin + 1e-12);
                alphaFin *= shrink;
                betaFin *= shrink;
            }
            if (betaFin < lower[3]) betaFin = lower[3];
            omegaFin = Math.max(omegaFin, omegaFloor);
        } catch (Throwable ignore) { /* segue com heurístico */ }

        // ----- multistart -----
        record Start(double mu, double omega, double alpha, double beta, double nu) {

            double[] asArray() {
                return new double[]{mu, omega, alpha, beta, nu};
            }
        }
        java.util.List<Start> starts = new java.util.ArrayList<>();
        if (omegaFin != null) {
            starts.add(new Start(mu0Raw, omegaFin, alphaFin, betaFin, 4.0));
            starts.add(new Start(mu0Raw, omegaFin, alphaFin, betaFin, 6.0));
            starts.add(new Start(mu0Raw, omegaFin, alphaFin, betaFin, 10.0));
        }
        // heurísticos plausíveis p/ FX H1
        starts.add(new Start(mu0Raw, Math.max(1.5 * omegaFloor, omegaFin != null ? 1.2 * omegaFin : 1.5 * omegaFloor), 0.08, 0.90, 6.0));
        starts.add(new Start(mu0Raw, Math.max(omegaFloor, 1e-6 * var0), 0.06, 0.92, 6.0));
        starts.add(new Start(mu0Raw, Math.max(omegaFloor, 5e-7 * var0), 0.10, 0.86, 8.0));

        // ----- NLL GARCH-t (Var z=1) + priors brandos -----
        ObjectiveFunction nll = new ObjectiveFunction(point -> {
            final double mu = point[0];
            final double omega = point[1];
            final double alpha = point[2];
            final double beta = point[3];
            final double nu = point[4];

            final double persist = alpha + beta;

            if (persist >= persistMax) return 1e12 + 1e10 * (persist - persistMax);
            if (nu <= 2.1) return 1e12 + 1e10 * (2.1 - nu);
            if (omega <= omegaFloor) return 1e12 + 1e8 * (omegaFloor - omega);

            // densidade t padronizada (Var=1)
            final double c = org.apache.commons.math3.special.Gamma.logGamma((nu + 1.0) / 2.0)
                    - org.apache.commons.math3.special.Gamma.logGamma(nu / 2.0)
                    - 0.5 * Math.log((nu - 2.0) * Math.PI);

            double nllVal = 0.0;
            double hPrev = Math.max(var0, omegaFloor);

            // primeira observação
            double eps0 = r[0] - mu;
            double z0 = eps0 / Math.sqrt(hPrev);
            nllVal += 0.5 * Math.log(hPrev)
                    - c + 0.5 * Math.log(nu - 2.0)
                    + ((nu + 1.0) / 2.0) * Math.log(1.0 + (z0 * z0) / (nu - 2.0));

            for (int t = 1; t < r.length; t++) {
                double epsPrev = r[t - 1] - mu;
                double h = omega + alpha * (epsPrev * epsPrev) + beta * hPrev;
                if (h < 1e-16) h = 1e-16;

                double eps = r[t] - mu;
                double z = eps / Math.sqrt(h);
                nllVal += 0.5 * Math.log(h)
                        - c + 0.5 * Math.log(nu - 2.0)
                        + ((nu + 1.0) / 2.0) * Math.log(1.0 + (z * z) / (nu - 2.0));

                hPrev = h;
            }

            // Priors/penalidades (ajuste #1: prior de persistência um pouco mais firme p/ H1)
            double penalty = 0.0;
            penalty += 40.0 * Math.pow(Math.max(0.0, 0.96 - persist), 2.0); // preferir α+β ≥ ~0.96 sem travar >0.995
            penalty += 5.0 * Math.pow(Math.max(0.0, 4.5 - nu), 2.0); // evita ν muito baixo
            penalty += 10.0 * Math.pow(mu / muCap, 2.0);                    // μ pequeno (ridge)

            // leve empurrão para longe das bordas de beta
            final double near = 1e-3;
            if (beta < lower[3] + near) penalty += 5.0 * Math.pow((lower[3] + near) - beta, 2.0);
            if (beta > upper[3] - near) penalty += 5.0 * Math.pow(beta - (upper[3] - near), 2.0);

            return nllVal + penalty;
        });

        // ---------- Otimização: CMA-ES (global) -> BOBYQA (refino) ----------
        final int maxEvalGlobal = 80_000;
        final int maxEvalLocal = 160_000;

        double bestNll = Double.POSITIVE_INFINITY;
        double[] bestP = null;

        for (Start st : starts) {
            double[] guess = st.asArray();
            // projeta guess nos bounds e repara α+β
            for (int i = 0; i < guess.length; i++) {
                guess[i] = Math.max(lower[i], Math.min(upper[i], guess[i]));
            }
            if (guess[2] + guess[3] >= persistMax) {
                double s = (persistMax - 1e-4) / (guess[2] + guess[3] + 1e-12);
                guess[2] *= s;
                guess[3] *= s;
            }

            double[] current = java.util.Arrays.copyOf(guess, guess.length);

            // (a) CMA-ES grosso (se disponível)
            try {
                final org.apache.commons.math3.random.RandomGenerator rng = new org.apache.commons.math3.random.Well44497b();
                rng.setSeed(42);

                org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer cma =
                        new org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer(
                                8000, 1e-9, true, 0, 0, rng, false, null);

                double[] sigma = {
                        0.25 * muCap,
                        Math.max(omegaFloor * 0.5, 1e-6),
                        0.03,
                        0.03,
                        2.0
                };

                org.apache.commons.math3.optim.PointValuePair solCMA = cma.optimize(
                        nll,
                        org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE,
                        new org.apache.commons.math3.optim.InitialGuess(current),
                        new org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer.Sigma(sigma),
                        new org.apache.commons.math3.optim.SimpleBounds(lower, upper),
                        new org.apache.commons.math3.optim.MaxEval(maxEvalGlobal),
                        new org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer.PopulationSize(30)
                );
                current = solCMA.getPoint();
            } catch (Throwable ignore) {
                // se CMA-ES indisponível, segue direto pro BOBYQA
            }

            // (b) Refino local BOBYQA
            try {
                BOBYQAOptimizer opt = new BOBYQAOptimizer(2 * current.length + 1);
                PointValuePair sol = opt.optimize(
                        nll,
                        GoalType.MINIMIZE,
                        new InitialGuess(current),
                        new SimpleBounds(lower, upper),
                        new MaxEval(maxEvalLocal)
                );
                double val = sol.getValue();
                if (val < bestNll) {
                    bestNll = val;
                    bestP = sol.getPoint();
                }
            } catch (Exception ignore) {
                // tenta próximo start
            }
        }

        if (bestP == null) {
            // fallback defensivo: usa finmath (normal) e ν=10 para seguir operando
            if (omegaFin != null) {
                bestP = new double[]{Math.max(-muCap, Math.min(muCap, mu0Raw)),
                        Math.max(omegaFloor, omegaFin),
                        Math.max(lower[2], Math.min(upper[2], alphaFin)),
                        Math.max(lower[3], Math.min(upper[3], betaFin)),
                        10.0};
            } else {
//                throw new IllegalStateException("Falha na estimação GARCH-t: nenhum start convergiu.");
                return Optional.empty();
            }
        }

        final double mu = bestP[0];
        final double omega = bestP[1];
        final double alpha = bestP[2];
        final double beta = bestP[3];
        final double nu = bestP[4];

        // (ajuste #2) alerta de baixa persistência
        final double persist = alpha + beta;
//        if (persist < 0.90) {
//            log.warn("Baixa persistência (α+β={} < 0.90) em {} — regime possivelmente transitório/ruidoso.", persist, timestamp);
//        }

        // logs de borda (diagnóstico)
//        if (near(beta, lower[3]) || near(beta, upper[3])) {
//            log.warn("β colado na borda em {}: beta={}", timestamp, beta);
//        }
//        if (near(nu, lower[4]) || near(nu, upper[4])) {
//            log.warn("ν colado na borda em {}: nu={}", timestamp, nu);
//        }

        // ---------- Forecast σ² ----------
        double[] hFilt = new double[r.length];
        hFilt[0] = Math.max(var0, omegaFloor);
        for (int t = 1; t < r.length; t++) {
            double epsPrev = r[t - 1] - mu;
            hFilt[t] = omega + alpha * (epsPrev * epsPrev) + beta * hFilt[t - 1];
            if (hFilt[t] < 1e-16) hFilt[t] = 1e-16;
        }
        double lastH = hFilt[hFilt.length - 1];
        double epsLast = r[r.length - 1] - mu;

        double[] sigma2Fwd = new double[horizonBars + 1];
        sigma2Fwd[1] = Math.max(omega + alpha * (epsLast * epsLast) + beta * lastH, 1e-16);
        for (int k = 2; k <= horizonBars; k++) {
            sigma2Fwd[k] = Math.max(omega + (alpha + beta) * sigma2Fwd[k - 1], 1e-16);
        }

        // σ(bar) (1-step) em pips
        final double sigmaPerBarPips = (pNow * Math.sqrt(sigma2Fwd[1])) / pipSize;

        // ---------- Quantis (μ=0 só aqui) ----------
        final double muForQ = 0.0;
        double tpPips, slPips;

        if (horizonBars == 1) {
            org.apache.commons.math3.distribution.TDistribution t = new org.apache.commons.math3.distribution.TDistribution(nu);
            double scaleStd = Math.sqrt((nu - 2.0) / nu);
            double qHiStd = scaleStd * t.inverseCumulativeProbability(1.0 - alphaTP);
            double qLoStd = scaleStd * t.inverseCumulativeProbability(alphaSL);

            double qHi = muForQ + Math.sqrt(sigma2Fwd[1]) * qHiStd;
            double qLo = muForQ + Math.sqrt(sigma2Fwd[1]) * qLoStd;

            tpPips = Math.abs(pNow * (Math.exp(qHi) - 1.0)) / pipSize;
            slPips = Math.abs(pNow * (Math.exp(qLo) - 1.0)) / pipSize;
        } else {
            int baseN = 20_000;
            if (sigmaPerBarPips > 15.0 || nu < 4.5) baseN = 50_000;
            if ((baseN & 1) == 1) baseN++;

            final int N = baseN;
            final java.util.SplittableRandom rng = new java.util.SplittableRandom(42L);
            final org.apache.commons.math3.distribution.TDistribution t = new org.apache.commons.math3.distribution.TDistribution(nu);
            final double scaleStd = Math.sqrt((nu - 2.0) / nu);
            final double[] agg = new double[N];

            int s = 0;
            while (s < N) {
                double u = GarchUtils.nextOpen01(rng);
                double uAnti = 1.0 - u;

                double z1 = scaleStd * t.inverseCumulativeProbability(u);
                double z2 = scaleStd * t.inverseCumulativeProbability(uAnti);

                agg[s++] = GarchUtils.simulatePathReturnSum(horizonBars, sigma2Fwd[1], muForQ, omega, alpha, beta, z1, t, scaleStd, rng);
                agg[s++] = GarchUtils.simulatePathReturnSum(horizonBars, sigma2Fwd[1], muForQ, omega, alpha, beta, z2, t, scaleStd, rng);
            }

            java.util.Arrays.sort(agg);
            double qLo = GarchUtils.quantileFromSorted(agg, alphaSL);
            double qHi = GarchUtils.quantileFromSorted(agg, 1.0 - alphaTP);

            tpPips = Math.abs(pNow * (Math.exp(qHi) - 1.0)) / pipSize;
            slPips = Math.abs(pNow * (Math.exp(qLo) - 1.0)) / pipSize;
        }

        // ---------- break-even hit rate ----------
        final double rr = tpPips / Math.max(slPips, 1e-12);
        final double breakEvenHitRate = 1.0 / (1.0 + rr);

        // ---------- sanity checks + logs (ajuste #3: manter checagens em σ(h)) ----------
        final double sigmaH = sigmaPerBarPips * Math.sqrt(horizonBars);
        final double tpSigma = tpPips / Math.max(sigmaH, 1e-9);
        final double slSigma = slPips / Math.max(sigmaH, 1e-9);

        TDistribution t = new TDistribution(nu);
        double scaleStd = Math.sqrt((nu - 2.0) / nu);   // t padronizada (Var=1)
        double zTP = scaleStd * t.inverseCumulativeProbability(1.0 - alphaTP); // >0
        double zSL = -scaleStd * t.inverseCumulativeProbability(alphaSL);       // >0

        // tolerâncias (±20% é um bom começo)
        double TP_SIGMA_MIN = 0.8 * zTP, TP_SIGMA_MAX = 1.2 * zTP;
        double SL_SIGMA_MIN = 0.8 * zSL, SL_SIGMA_MAX = 1.2 * zSL;

//        if (alphaTP < alphaSL && tpPips < slPips) {
//            log.warn("Quantis invertidos (TP<SL) em {}. rr={}, tp={}, sl={}", timestamp, rr, tpPips, slPips);
//        }
//        if (tpSigma < TP_SIGMA_MIN || tpSigma > TP_SIGMA_MAX) {
//            log.warn("TP_σ fora da faixa [{},{}] em {}: tpSigma={}", TP_SIGMA_MIN, TP_SIGMA_MAX, timestamp, tpSigma);
//        }
//        if (slSigma < SL_SIGMA_MIN) {
//            log.warn("SL_σ muito curto (<{}) em {}: slSigma={}. Risco de stop-out elevado.", SL_SIGMA_MIN, timestamp, slSigma);
//        }
//        if (slSigma > SL_SIGMA_MAX) {
//            log.warn("SL_σ muito longo (>{}) em {}: slSigma={}. Risco/retorno pode ficar ineficiente.", SL_SIGMA_MAX, timestamp, slSigma);
//        }
//        if (sigmaH > 60.0) {
//            log.warn("σ(h) muito alto (>60 pips) em {}: sigmaH={} pips. Considere reduzir lote/NO-GO.", timestamp, sigmaH);
//        }

        return Optional.of(new GarchDto(
                timestamp,
                tpPips,
                slPips,
                sigmaPerBarPips,
                mu,
                omega,
                alpha,
                beta,
                nu,
                breakEvenHitRate,
                persist < 0.90,
                GarchUtils.near(beta, lower[3]) || GarchUtils.near(beta, upper[3]),
                GarchUtils.near(nu, lower[4]) || GarchUtils.near(nu, upper[4]),
                alphaTP < alphaSL && tpPips < slPips,
                tpSigma < TP_SIGMA_MIN || tpSigma > TP_SIGMA_MAX,
                slSigma < SL_SIGMA_MIN,
                Double.isFinite(slSigma) && slSigma > SL_SIGMA_MAX,
                sigmaH > 60.0
        ));
    }
}
