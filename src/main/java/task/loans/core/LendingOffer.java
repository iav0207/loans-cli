package task.loans.core;

import java.math.BigDecimal;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang3.builder.ToStringBuilder;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Core entity: an offer to lend some money to a prospective borrower.
 * <p>
 * Instances of this class are self-validated and immutable.
 */
@ParametersAreNonnullByDefault
public class LendingOffer {

    private final String lenderName;
    private final BigDecimal rate;
    private final BigDecimal amount;

    /**
     * Create instance.
     *
     * @param lenderName    string representing the lender's name, non-null, not blank, any characters.
     * @param rate          <i>annual</i> interest rate, non-null, non-negative.
     * @param amount        amount of money offered, non-null, non-negative.
     */
    public LendingOffer(String lenderName, BigDecimal rate, BigDecimal amount) {
        this.lenderName = lenderName;
        this.rate = rate;
        this.amount = amount;
        validate();
    }

    private void validate() {
        checkArgument(lenderName != null, "Lender name must be non-null");
        checkArgument(!isBlank(lenderName), "Lender name should not be blank");

        checkArgument(rate != null, "Rate must be non-null");
        checkArgument(rate.compareTo(BigDecimal.ZERO) >= 0, "Rate value must be non-negative");

        checkArgument(amount != null, "Available money must be non-null");
        checkArgument(amount.compareTo(BigDecimal.ZERO) >= 0,
                "Available money amount must be non-negative");
    }

    /**
     * @return Lender's name.
     */
    public String getLenderName() {
        return lenderName;
    }

    /**
     * @return Annual interest rate.
     */
    public BigDecimal getRate() {
        return rate;
    }

    /**
     * @return Amount of money offered for lending.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LendingOffer that = (LendingOffer) o;
        return Objects.equals(lenderName, that.lenderName) &&
                Objects.equals(rate, that.rate) &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lenderName, rate, amount);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("lenderName", lenderName)
                .append("rate", rate)
                .append("amount", amount)
                .toString();
    }
}
