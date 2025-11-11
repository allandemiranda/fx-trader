package br.allandemiranda.fx.utils;

import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.math3.distribution.TDistribution;

import java.util.SplittableRandom;

@UtilityClass
public final class GarchUtils {

    public static double mean(double @NotNull [] x) {
        double s = 0.0;
        for (double v : x) s += v;
        return s / x.length;
    }

    public static double variance(double @NotNull [] x, double m) {
        double s = 0.0;
        for (double v : x) {
            double d = v - m;
            s += d * d;
        }
        return s / (x.length - 1);
    }

    public static double quantileFromSorted(double[] sorted, double p) {
        if (p <= 0) return sorted[0];
        if (p >= 1) return sorted[sorted.length - 1];
        double idx = p * (sorted.length - 1);
        int i = (int) Math.floor(idx);
        double w = idx - i;
        return sorted[i] * (1 - w) + sorted[Math.min(i + 1, sorted.length - 1)] * w;
    }

    public static boolean near(double x, double b) {
        return Math.abs(x - b) <= 1e-6 * Math.max(1.0, Math.abs(b));
    }

    public static double simulatePathReturnSum(int h, double h1Ahead, double muForQ, double omega, double alpha, double beta, double zStdFirst, TDistribution t, double scaleStd, SplittableRandom rng) {
        double hNext = h1Ahead;
        double retSum = 0.0;

        for (int k = 1; k <= h; k++) {
            double zStd = (k == 1)
                    ? zStdFirst
                    : scaleStd * t.inverseCumulativeProbability(nextOpen01(rng));

            double eps = Math.sqrt(hNext) * zStd;
            retSum += (muForQ + eps);

            if (k < h) {
                hNext = omega + alpha * (eps * eps) + beta * hNext;
                if (hNext < 1e-16) hNext = 1e-16;
            }
        }
        return retSum;
    }

    public static double nextOpen01(@NotNull SplittableRandom rng) {
        double u;
        do {
            u = rng.nextDouble();
        } while (u <= 0.0 || u >= 1.0);
        return u;
    }
}
