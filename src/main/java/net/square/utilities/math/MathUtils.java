package net.square.utilities.math;

import java.util.Collection;

public class MathUtils {

    public static final double EXPANDER = Math.pow(2, 24);

    public static double getVariance(final Collection<? extends Number> data) {
        double average = getAverage(data);
        double variance = 0.0;
        for (final Number number : data) {
            variance += Math.pow(number.doubleValue() - average, 2.0);
        }

        return variance;
    }

    public static double getStandardDeviation(final Collection<? extends Number> data) {
        final double variance = getVariance(data);
        return Math.sqrt(variance);
    }

    public static double getAverage(final Collection<? extends Number> data) {
        double sum = 0;
        int count = 0;
        for (Number number : data) {
            sum += number.doubleValue();
            count++;
        }
        return count > 0 ? sum / count : 0D;
    }

    public static long getVictim(final long a, final long b) {
        return (b <= 16384L) ? a : getVictim(b, a % b);
    }

    public static long getGcd(final long current, final long previous) {
        return (previous <= 16384L) ? current : getGcd(previous, current % previous);
    }
}
