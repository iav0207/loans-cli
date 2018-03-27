package task.loans.core;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Decimal arithmetic context.
 */
@ParametersAreNonnullByDefault
public class Money {

    private Money() {
        // no instantiation
    }

    /**
     * Default decimal arithmetic standard for monetary calculations.
     * Rounding mode: {@link RoundingMode#HALF_EVEN}.
     */
    static final MathContext MONEY_CONTEXT = MathContext.DECIMAL128;

    static final int CENT_SCALE = 2;
    static final int RATE_SCALE = 3;

    /**
     * Factory method for creating {@link BigDecimal} values in one money context.
     *
     * @param val integer value
     * @return {@link BigDecimal} money value with scale {@value CENT_SCALE}.
     */
    public static BigDecimal decimal(int val) {
        return new BigDecimal(val).setScale(CENT_SCALE, roundingMode());
    }

    /**
     * Factory method for creating {@link BigDecimal} values in one money context.
     *
     * @param val string value
     * @return {@link BigDecimal} money value with scale {@value CENT_SCALE}.
     */
    public static BigDecimal decimal(String val) {
        return new BigDecimal(val).setScale(CENT_SCALE, roundingMode());
    }

    /**
     * Factory method for creating {@link BigDecimal} rate value.
     *
     * @param val string value
     * @return {@link BigDecimal} money value with scale {@value RATE_SCALE}.
     */
    public static BigDecimal rate(String val) {
        return new BigDecimal(val).setScale(RATE_SCALE, roundingMode());
    }

    /**
     * @return Default money-compatible rounding mode: {@link RoundingMode#HALF_EVEN}.
     */
    static RoundingMode roundingMode() {
        return MONEY_CONTEXT.getRoundingMode();
    }

    /**
     * @return {@code true} if the numbers are equal disregarding to {@link BigDecimal#scale()},
     * {@code false} otherwise.
     */
    static boolean numericallyEqual(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) == 0;
    }
}
