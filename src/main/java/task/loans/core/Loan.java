package task.loans.core;

import java.math.BigDecimal;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang3.builder.ToStringBuilder;

import static java.util.Objects.requireNonNull;
import static task.loans.core.Money.decimal;

/**
 * Core entity: loan.
 * Instances of this class are immutable.
 */
@ParametersAreNonnullByDefault
public class Loan {

    private static final BigDecimal MINUS_ONE = decimal(-1);

    private final BigDecimal requestedAmount;
    private final BigDecimal rate;
    private final BigDecimal monthlyRepayment;
    private final BigDecimal totalRepayment;

    /**
     * Create instance.
     */
    private Loan(Builder builder) {
        this.requestedAmount = requireNonNull(builder.requestedAmount);
        this.rate = requireNonNull(builder.rate);
        this.monthlyRepayment = requireNonNull(builder.monthlyRepayment);
        this.totalRepayment = requireNonNull(builder.totalRepayment);
    }

    /**
     * Create instance representing a situation when no loan of the specified value can be provided.
     *
     * @param requestedAmount amount of loan requested by a borrower
     * @return An instance holding the requested loan amount and the special value
     * of {@link #MINUS_ONE} for the rest of the fields, i.e. repayments and rate.
     */
    public static Loan unavailable(BigDecimal requestedAmount) {
        return builder()
                .requestedAmount(requestedAmount)
                .monthlyRepayment(MINUS_ONE)
                .totalRepayment(MINUS_ONE)
                .rate(MINUS_ONE).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    /**
     * @return Effective interest rate (decimal value)
     * or {@link #MINUS_ONE} – if loan for the requested amount cannot be provided.
     */
    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getMonthlyRepayment() {
        return monthlyRepayment;
    }

    public BigDecimal getTotalRepayment() {
        return totalRepayment;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("requestedAmount", requestedAmount)
                .append("rate", rate)
                .append("monthlyRepayment", monthlyRepayment)
                .append("totalRepayment", totalRepayment)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Loan loan = (Loan) o;
        return Objects.equals(requestedAmount, loan.requestedAmount) &&
                Objects.equals(rate, loan.rate) &&
                Objects.equals(monthlyRepayment, loan.monthlyRepayment) &&
                Objects.equals(totalRepayment, loan.totalRepayment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestedAmount, rate, monthlyRepayment, totalRepayment);
    }

    public static final class Builder {
        private BigDecimal requestedAmount;
        private BigDecimal rate;
        private BigDecimal monthlyRepayment;
        private BigDecimal totalRepayment;

        private Builder() {
        }

        public Builder requestedAmount(BigDecimal requestedAmount) {
            this.requestedAmount = requestedAmount;
            return this;
        }

        public Builder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }

        public Builder monthlyRepayment(BigDecimal monthlyRepayment) {
            this.monthlyRepayment = monthlyRepayment;
            return this;
        }

        public Builder totalRepayment(BigDecimal totalRepayment) {
            this.totalRepayment = totalRepayment;
            return this;
        }

        public Loan build() {
            return new Loan(this);
        }
    }
}
