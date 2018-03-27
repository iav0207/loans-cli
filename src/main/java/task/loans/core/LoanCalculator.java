package task.loans.core;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.util.streamex.EntryStream;
import javax.util.streamex.StreamEx;

import com.google.common.collect.ImmutableSortedMap;

import static com.google.common.base.Preconditions.checkArgument;
import static task.loans.core.Money.CENT_SCALE;
import static task.loans.core.Money.MONEY_CONTEXT;
import static task.loans.core.Money.decimal;
import static task.loans.core.Money.numericallyEqual;
import static task.loans.core.Money.roundingMode;

@ParametersAreNonnullByDefault
public class LoanCalculator {

    private static final int MONTHS_IN_YEAR = 12;

    /**
     * Number of monthly payments.
     */
    private static final int REPAYMENTS = 36;

    private final SortedMap<BigDecimal, BigDecimal> offers;
    private final BigDecimal totalSupply;

    /**
     * Create an immutable instance and prepare data structure for further calculations.
     * Time complexity: O(n*log(n)).
     *
     * @param offers list of offers from the lenders, i.e. market data.
     */
    public LoanCalculator(List<LendingOffer> offers) {
        SortedMap<BigDecimal, BigDecimal> map = StreamEx.of(offers)
                .mapToEntry(LendingOffer::getRate, LendingOffer::getAmount)
                .toSortedMap(BigDecimal::add);

        this.offers = ImmutableSortedMap.copyOfSorted(map);
        this.totalSupply = offers.stream().map(LendingOffer::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate compound loan of the specified amount satisfied by the offers.
     * <p>
     * The loan returned will have as low a rate as is possible.
     *
     * @param requestedAmount total amount of loan requested, non-negative value
     * @return {@link Loan} instance with calculated compound rate and repayment amounts,
     * or {@link Loan#unavailable} – if the request cannot be satisfied by the market.
     */
    public Loan calculate(BigDecimal requestedAmount) {
        checkArgument(requestedAmount.compareTo(BigDecimal.ZERO) >= 0, "Loan amount must be non-negative");
        if (totalSupply.compareTo(requestedAmount) < 0) {
            return Loan.unavailable(requestedAmount);
        }
        return new InternalCalculator(requestedAmount).getResult();
    }

    private class InternalCalculator {
        private final BigDecimal requestedAmount;
        private final Map<BigDecimal, BigDecimal> loans;

        InternalCalculator(BigDecimal requestedAmount) {
            this.requestedAmount = requestedAmount;
            this.loans = createLoansMap(requestedAmount);
        }

        Loan getResult() {
            BigDecimal monthlyRepayment = getMonthlyRepayment();
            return Loan.builder()
                    .requestedAmount(requestedAmount)
                    .monthlyRepayment(monthlyRepayment)
                    .totalRepayment(monthlyRepayment.multiply(decimal(REPAYMENTS))
                            .setScale(CENT_SCALE, roundingMode()))
                    .rate(calculateEffectiveAnnualRate())
                    .build();
        }

        /**
         * Build compound loan table.
         *
         * @param requestedAmount amount requested by a borrower
         * @return Map: rate -> amount, with sum of values equal to requested amount.
         */
        private Map<BigDecimal, BigDecimal> createLoansMap(BigDecimal requestedAmount) {
            Map<BigDecimal, BigDecimal> map = new HashMap<>();
            BigDecimal need = requestedAmount;
            for (Map.Entry<BigDecimal, BigDecimal> offer : offers.entrySet()) {
                BigDecimal offeredAmount = offer.getValue();
                BigDecimal amountToTake = offeredAmount.min(need);
                map.put(offer.getKey(), amountToTake);
                need = need.subtract(amountToTake);
                if (numericallyEqual(need, BigDecimal.ZERO)) {
                    break;
                }
            }
            return map;
        }

        private BigDecimal getMonthlyRepayment() {
            return EntryStream.of(loans)
                    .mapKeys(this::toMonthlyInterestRate)
                    .invert()
                    .mapKeyValue(this::calculateMonthlyRepayment)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(Money.CENT_SCALE, Money.roundingMode());
        }

        /**
         * Calculate monthly repayment having principal amount,
         * interest rate per repayment period (one month), and number of repayments ({@value #REPAYMENTS}).
         *
         * @param principal             principal amount, P
         * @param monthlyInterestRate   monthly interest rate, Rm
         * @return {@code Rm * P * r / (r-1)},<br/>
         * where {@code r = (1+Rm)^N}, N – number of repayments
         */
        private BigDecimal calculateMonthlyRepayment(BigDecimal principal, BigDecimal monthlyInterestRate) {
            if (monthlyInterestRate.compareTo(BigDecimal.ZERO) == 0) {
                return principal.divide(decimal(REPAYMENTS), MONEY_CONTEXT);
            }
            BigDecimal r = BigDecimal.ONE.add(monthlyInterestRate).pow(REPAYMENTS);
            return monthlyInterestRate.multiply(principal).multiply(r)
                    .divide(r.subtract(BigDecimal.ONE), MONEY_CONTEXT)
                    .setScale(CENT_SCALE, roundingMode());
        }

        /**
         * Weighted (by amount) average of the rates from the loans table.
         */
        private BigDecimal calculateEffectiveAnnualRate() {
            BigDecimal weightedSum = EntryStream.of(loans)
                    .mapKeyValue(BigDecimal::multiply)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return weightedSum.divide(requestedAmount, roundingMode())
                    .setScale(Money.RATE_SCALE, roundingMode());
        }

        /**
         * Rm = Ry / {@value MONTHS_IN_YEAR}
         *
         * @param annualInterestRate annual interest rate
         * @return Monthly interest rate.
         */
        private BigDecimal toMonthlyInterestRate(BigDecimal annualInterestRate) {
            return annualInterestRate.divide(decimal(MONTHS_IN_YEAR), MONEY_CONTEXT);
        }
    }
}
