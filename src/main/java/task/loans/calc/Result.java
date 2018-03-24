package task.loans.calc;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.io.IOUtils;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Successful lending repayment calculation 
 * Encapsulates output formatting.
 */
@ParametersAreNonnullByDefault
public class Result {

    private static final String FORMAT = readFormatFromResource("result_format.txt");

    private static final String FAIL_MESSAGE = "Lending for the specified amount is currently unavailable";

    private static final Result FAILED = new Result();

    final BigDecimal requestedAmount;
    final BigDecimal rate;
    final BigDecimal monthlyRepayment;
    final BigDecimal totalRepayment;

    private final String stringRepresentation;

    /**
     * Result: success. Initializing with calculated values.
     */
    private Result(Builder builder) {
        this.requestedAmount = requireNonNull(builder.requestedAmount);
        this.rate = requireNonNull(builder.rate);
        this.monthlyRepayment = requireNonNull(builder.monthlyRepayment);
        this.totalRepayment = requireNonNull(builder.totalRepayment);

        this.stringRepresentation = format(FORMAT, requestedAmount, rate, monthlyRepayment, totalRepayment);
    }

    /**
     * Result: lending failed.
     */
    private Result() {
        BigDecimal minusOne = BigDecimal.ONE.negate();
        this.requestedAmount = minusOne;
        this.rate = minusOne;
        this.monthlyRepayment = minusOne;
        this.totalRepayment = minusOne;

        this.stringRepresentation = FAIL_MESSAGE;
    }

    /**
     * @return Builder for the new successful result.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Result: lending failed.
     */
    static Result lendingFailed() {
        return FAILED;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Result result = (Result) o;
        return Objects.equals(requestedAmount, result.requestedAmount) &&
                Objects.equals(rate, result.rate) &&
                Objects.equals(monthlyRepayment, result.monthlyRepayment) &&
                Objects.equals(totalRepayment, result.totalRepayment) &&
                Objects.equals(stringRepresentation, result.stringRepresentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestedAmount, rate, monthlyRepayment, totalRepayment, stringRepresentation);
    }

    private static String readFormatFromResource(String res) {
        try {
            return String.join("",
                    IOUtils.readLines(Result.class.getResourceAsStream(res), "utf-8"));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    static final class Builder {
        private BigDecimal requestedAmount;
        private BigDecimal rate;
        private BigDecimal monthlyRepayment;
        private BigDecimal totalRepayment;

        private Builder() {
        }

        Builder requestedAmount(BigDecimal requestedAmount) {
            this.requestedAmount = requestedAmount;
            return this;
        }

        Builder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }

        Builder monthlyRepayment(BigDecimal monthlyRepayment) {
            this.monthlyRepayment = monthlyRepayment;
            return this;
        }

        Builder totalRepayment(BigDecimal totalRepayment) {
            this.totalRepayment = totalRepayment;
            return this;
        }

        Result build() {
            return new Result(this);
        }
    }
}
