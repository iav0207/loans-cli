package task.loans.model;

import java.math.BigDecimal;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class LendingOffer {

    private final String lenderName;
    private final BigDecimal rate;
    private final BigDecimal amount;

    public LendingOffer(String lenderName, BigDecimal rate, BigDecimal amount) {
        this.lenderName = lenderName;
        this.rate = rate;
        this.amount = amount;
        validate();
    }

    private void validate() {
        checkArgument(lenderName != null, "Lender name must be non-null");
        checkArgument(rate != null, "Rate must be non-null");
        checkArgument(amount != null, "Available money must be non-null");

        checkArgument(!isBlank(lenderName), "Lender name should not be blank");
        checkArgument(rate.compareTo(BigDecimal.ZERO) >= 0, "Rate must be non-negative");
        checkArgument(amount.compareTo(BigDecimal.ZERO) >= 0,
                "Available money amount must be non-negative");
    }

    public String getLenderName() {
        return lenderName;
    }

    public BigDecimal getRate() {
        return rate;
    }

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
