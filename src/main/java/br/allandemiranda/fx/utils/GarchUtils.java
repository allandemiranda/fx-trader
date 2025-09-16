package br.allandemiranda.fx.utils;

import br.allandemiranda.fx.dto.CandlestickDto;
import lombok.experimental.UtilityClass;
import net.finmath.timeseries.models.parametric.GARCH;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.special.Gamma;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

/**
 * Finmath-backed GARCH(1,1) with unit-variance standardized Student-t innovations.
 * <p>
 * Pipeline:
 * 1) Fit (omega, alpha, beta) with finmath (Gaussian MLE) from PRICES.
 * 2) Compute mu as the mean of log-returns (skipping r[0]).
 * 3) Rebuild sigma^2, sigma, eps, z.
 * 4) Estimate nu (degrees of freedom) via 1D MLE on z (holding omega, alpha, beta, mu fixed).
 * <p>
 * Note: This is NOT joint t-Student MLE like your original GarchTEngine. If you need that,
 * use this adapter only for seeding and then run your t-likelihood optimizer.
 */
@UtilityClass
public final class GarchUtils {
    // ---------- Safety constants ----------
    private static final double EPS_VAR = 1e-12;   // floor for variance
    private static final double EPS_SIGMA = 1e-14;   // floor for sigma^2 in recursion
    private static final double PHI_MAX = 0.998;   // enforce alpha + beta < PHI_MAX
    private static final double NU_MIN = 2.05;
    private static final double NU_MAX = 200.0;

    // ---------- Public API (matches your main) ----------

    /**
     * Log-returns from candle closes; keeps r[0] = 0.0 for compatibility with your main.
     */
    public static double[] toLogReturnsClose(List<CandlestickDto> candles) {
        final int n = candles.size();
        if (n < 2) throw new IllegalArgumentException("Need at least 2 candles.");
        double[] r = new double[n];
        r[0] = 0.0;
        for (int i = 1; i < n; i++) {
            double p = candles.get(i).close().doubleValue();
            double pPrev = candles.get(i - 1).close().doubleValue();
            if (!isFinite(p) || !isFinite(pPrev) || pPrev <= 0.0) {
                throw new IllegalArgumentException("Non-finite or non-positive price encountered.");
            }
            r[i] = Math.log(p / pPrev);
        }
        return r;
    }

    /**
     * Fit via finmath (omega, alpha, beta) + 1D MLE for nu; rebuild series.
     */
    public static Result fitGarchT(List<CandlestickDto> candles) {
        // 1) Fit (omega, alpha, beta) from PRICES via finmath.
        final double[] prices = candles.stream().map(CandlestickDto::close).mapToDouble(BigDecimal::doubleValue).toArray();
        if (prices.length < 2) throw new IllegalArgumentException("Need at least 2 prices.");
        for (double v : prices) {
            if (!isFinite(v) || v <= 0.0) {
                throw new IllegalArgumentException("Non-finite or non-positive price encountered.");
            }
        }

        final GARCH fm = new GARCH(prices);
        final OmegaAlphaBeta p = readGarchParamsWithGuess(fm, prices);

        // 2) Returns and mu (mean of returns, skipping r[0] — matches your code).
        final double[] r = toLogReturnsClose(candles);
        final double mu = mean(r, 1, r.length);

        // 3) Stabilize parameters (floors and phi < PHI_MAX).
        double omega = max(EPS_VAR, p.omega);
        double alpha = max(0.0, p.alpha);
        double beta = max(0.0, p.beta);
        double phi = alpha + beta;
        if (phi >= PHI_MAX) { // project proportionally into the interior
            double scale = (PHI_MAX - 1e-6) / max(1e-12, phi);
            alpha *= scale;
            beta *= scale;
            phi = alpha + beta;
        }

        // 4) Rebuild eps / sigma^2 / sigma / z
        final Series s = buildSeries(r, omega, alpha, beta, mu);

        // 5) Adaptive burn-in for nu (phi → 1 ⇒ longer burn)
        final int n = r.length;
        final int burn = Math.min(n - 1, Math.max(10, (int) Math.ceil(5.0 / max(1e-6, (1.0 - phi)))));

        // 6) 1D MLE for nu (standardized t with Var = 1), using only z
        final double nu = estimateNuMLE(s.z, burn);

        // 7) Unconditional variance
        final double uncondVar = omega / max(EPS_VAR, 1.0 - phi);

        return new Result(
                omega, alpha, beta, mu, nu, uncondVar,
                s.sigma2, s.sigma, s.eps, s.z
        );
    }

    /**
     * h-step-ahead variance forecast; uses closed-form for h ≥ 2.
     */
    public static double[] forecastVariance(Result res, int h) {
        if (h < 1) throw new IllegalArgumentException("h >= 1");
        final double[] f = new double[h];
        final double omega = res.omega();
        final double alpha = res.alpha();
        final double beta = res.beta();
        final double phi = alpha + beta;
        final double lastSigma2 = res.lastSigma2();
        final double eLast = res.residuals()[res.residuals().length - 1];
        final double lastEps2 = eLast * eLast;

        // h = 1
        f[0] = omega + alpha * lastEps2 + beta * lastSigma2;

        if (h >= 2) {
            // Closed form: vbar + phi^i * (f0 - vbar)
            final double vbar = omega / max(EPS_VAR, 1.0 - phi);
            for (int i = 1; i < h; i++) {
                f[i] = vbar + Math.pow(phi, i) * (f[0] - vbar);
            }
        }
        return f;
    }

    /**
     * One-sided quantile magnitude for unit-variance standardized t_ν at confidence c (e.g., 0.99).
     */
    public static double tQuantileUnitVariance(double c, double nu) {
        if (nu <= 2) throw new IllegalArgumentException("nu must be > 2");
        TDistribution t = new TDistribution(nu);
        double qStd = t.inverseCumulativeProbability(c); // standard t (Var = nu/(nu-2))
        double s = sqrt(nu / (nu - 2.0));
        return abs(qStd) / s; // convert to unit-variance
    }

    /**
     * VaR (return space) for confidence c, given sigma_t and nu.
     */
    public static double varOneBar(double sigma_t, double c, double nu) {
        return tQuantileUnitVariance(c, nu) * sigma_t;
    }

    /**
     * Price distance for stop/take at horizon h using forecast variance (sigmaH is return stddev).
     */
    public static double distancePrice(double lastPrice, double k, double sigmaH) {
        return k * sigmaH * lastPrice;
    }

    /**
     * Convert return stddev to pips (e.g., EURUSD → 1e4; JPY pairs → 1e2).
     */
    public static double toPips(double stdReturn, double price, double pipFactor) {
        return stdReturn * price * pipFactor;
    }

    // ---------- Internals ----------

    private static Series buildSeries(double[] r, double omega, double alpha, double beta, double mu) {
        final int n = r.length;

        // eps = r - mu
        double[] eps = new double[n];
        for (int t = 0; t < n; t++) eps[t] = r[t] - mu;

        // sample variance of eps (for robust initialization fallback)
        double meanEps = mean(eps, 1, n);
        double var = variance(eps, 1, n, meanEps);

        double[] sigma2 = new double[n];
        double phi = alpha + beta;
        double uncond = (phi < PHI_MAX)
                ? omega / max(EPS_VAR, 1.0 - phi)
                : max(1e-8, var);

        sigma2[0] = max(EPS_VAR, uncond);
        for (int t = 1; t < n; t++) {
            double prev = sigma2[t - 1];
            double e = eps[t - 1];
            double e2 = e * e;
            double s2 = omega + alpha * e2 + beta * prev;
            sigma2[t] = Math.max(s2, EPS_SIGMA);
        }

        double[] sigma = new double[n];
        double[] z = new double[n];
        for (int t = 0; t < n; t++) {
            double s = sqrt(sigma2[t]);
            sigma[t] = s;
            z[t] = (s > 0) ? (eps[t] / s) : 0.0;
        }
        return new Series(sigma2, sigma, eps, z);
    }

    /**
     * 1D MLE for nu with unit-variance standardized Student-t using only z.
     * Burn-in (number of initial observations to skip) is passed in and usually adaptive to phi.
     */
    private static double estimateNuMLE(double[] z, int burn) {
        final int n = z.length;
        final int useFrom = Math.min(n - 1, Math.max(1, burn)) + 1;

        UnivariateObjectiveFunction obj = new UnivariateObjectiveFunction((nu) -> {
            if (nu <= 2.0) return 1e12;
            // constants for unit-variance t (we already standardized by sigma, so no -0.5*log(sigma2))
            double c1 = Gamma.logGamma((nu + 1.0) / 2.0) - Gamma.logGamma(nu / 2.0)
                    - 0.5 * Math.log((nu - 2.0) * Math.PI);
            double ll = 0.0;
            for (int t = useFrom; t < n; t++) {
                double zi = z[t];
                ll += c1 - ((nu + 1.0) / 2.0) * Math.log(1.0 + (zi * zi) / (nu - 2.0));
            }
            // mild penalty near the boundary to smooth the landscape
            if (nu < 2.2) ll -= 1e3 * (2.2 - nu) * (2.2 - nu);
            return -ll; // minimize negative log-likelihood
        });

        BrentOptimizer bo = new BrentOptimizer(1e-6, 1e-10);
        var sol = bo.optimize(
                new MaxEval(10_000),
                obj,
                GoalType.MINIMIZE,
                new SearchInterval(NU_MIN, NU_MAX, 8.0)
        );
        double nuHat = sol.getPoint();
        if (!isFinite(nuHat) || nuHat <= 2.0) nuHat = 8.0;
        return max(NU_MIN, min(NU_MAX, nuHat));
    }

    /**
     * Extract (omega, alpha, beta) from finmath with a sensible guess derived from the series.
     * Uses getBestParameters(guess) and tolerates different key casings (Omega/Alpha/Beta).
     */
    private static OmegaAlphaBeta readGarchParamsWithGuess(GARCH fm, double[] prices) {
        // Build a quick guess from the sample variance of log-returns.
        final double[] r = new double[prices.length];
        r[0] = 0.0;
        for (int i = 1; i < prices.length; i++) r[i] = Math.log(prices[i] / prices[i - 1]);
        final double mu = mean(r, 1, r.length);
        final double var = variance(r, 1, r.length, mu);

        Map<String, Object> guess = new java.util.HashMap<>();
        // Use a coarse heuristic omega ≈ var * 0.05, alpha=0.05, beta=0.90
        guess.put("Omega", Math.max(1e-8, var * 0.05));
        guess.put("Alpha", 0.05);
        guess.put("Beta", 0.90);

        Object ret = fm.getBestParameters(guess); // let finmath start from a sensible place

        if (ret instanceof Map<?, ?> raw) {
            Double omega = null, alpha = null, beta = null;
            for (Map.Entry<?, ?> e : raw.entrySet()) {
                String k = (e.getKey() == null) ? "" : e.getKey().toString().trim().toLowerCase();
                Object v = e.getValue();
                if (v instanceof Number num) {
                    switch (k) {
                        case "omega" -> omega = num.doubleValue();
                        case "alpha" -> alpha = num.doubleValue();
                        case "beta" -> beta = num.doubleValue();
                        default -> { /* ignore Vol, Szenarios, Likelihood, Quantile=... */ }
                    }
                }
            }
            // Fallbacks for capitalized keys (as seen in your logs)
            if (omega == null && raw.get("Omega") instanceof Number n1) omega = n1.doubleValue();
            if (alpha == null && raw.get("Alpha") instanceof Number n2) alpha = n2.doubleValue();
            if (beta == null && raw.get("Beta") instanceof Number n3) beta = n3.doubleValue();

            if (omega != null && alpha != null && beta != null) {
                return new OmegaAlphaBeta(omega, alpha, beta);
            }
            throw new IllegalStateException(
                    "finmath#getBestParameters did not provide Omega/Alpha/Beta. Keys=" + raw.keySet() + " map=" + raw
            );
        }

        // Very defensive: try getValues() if some older shape returns a custom object.
        try {
            var m = ret.getClass().getMethod("getValues");
            Object valuesObj = m.invoke(ret);
            if (valuesObj instanceof double[] v && v.length >= 3) {
                return new OmegaAlphaBeta(v[0], v[1], v[2]);
            }
        } catch (Exception ignore) {
        }

        throw new IllegalStateException("Unsupported return from finmath#getBestParameters: "
                + (ret == null ? "null" : ret.getClass().getName()));
    }

    // ---------- Numeric helpers ----------

    private static boolean isFinite(double x) {
        return !Double.isNaN(x) && !Double.isInfinite(x);
    }

    private static double mean(double[] a, int from, int to) {
        double s = 0.0;
        int c = 0;
        for (int i = from; i < to; i++) {
            s += a[i];
            c++;
        }
        return c > 0 ? s / c : 0.0;
    }

    private static double variance(double[] a, int from, int to, double mean) {
        double v = 0.0;
        int c = 0;
        for (int i = from; i < to; i++) {
            double d = a[i] - mean;
            v += d * d;
            c++;
        }
        return Math.max(0.0, v / Math.max(1, c - 1));
    }

    // ---------- Small containers ----------

    private record Series(double[] sigma2, double[] sigma, double[] eps, double[] z) {
    }

    private record OmegaAlphaBeta(double omega, double alpha, double beta) {
    }

    public record Result(
            double omega,
            double alpha,
            double beta,
            double mu,
            double nu,
            double unconditionalVariance,
            double[] sigma2,
            double[] sigma,
            double[] residuals,
            double[] z
    ) {
        public double lastSigma() {
            return sigma[sigma.length - 1];
        }

        public double lastSigma2() {
            return sigma2[sigma2.length - 1];
        }

        public double phi() {
            return alpha + beta;
        }
    }
}
