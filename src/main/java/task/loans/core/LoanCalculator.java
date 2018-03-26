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

    public LoanCalculator(List<LendingOffer> offers) {
        SortedMap<BigDecimal, BigDecimal> map = StreamEx.of(offers)
                .mapToEntry(LendingOffer::getRate, LendingOffer::getAmount)
                .toSortedMap(BigDecimal::add);

        this.offers = ImmutableSortedMap.copyOfSorted(map);
        this.totalSupply = offers.stream().map(LendingOffer::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Loan calculate(BigDecimal lendingAmount) {
        if (totalSupply.compareTo(lendingAmount) < 0) {
            return Loan.unavailable(lendingAmount);
        }
        return new InternalCalculator(lendingAmount).getResult();
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
                    .totalRepayment(monthlyRepayment.multiply(decimal(REPAYMENTS)))
                    .rate(calculateEffectiveRate())
                    .build();
        }

        /**
         * Build compound loan table.
         *
         * @return Map: rate -> amount.
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

        private BigDecimal calculateMonthlyRepayment(BigDecimal principal, BigDecimal monthlyInterestRate) {
            if (monthlyInterestRate.compareTo(BigDecimal.ZERO) == 0) {
                return principal.divide(decimal(REPAYMENTS), MONEY_CONTEXT);
            }
            BigDecimal r = BigDecimal.ONE.add(monthlyInterestRate).pow(REPAYMENTS);
            return monthlyInterestRate.multiply(principal).multiply(r)
                    .divide(r.subtract(BigDecimal.ONE), MONEY_CONTEXT)
                    .setScale(CENT_SCALE, roundingMode());
        }

        private BigDecimal calculateEffectiveRate() {
            BigDecimal weightedSum = EntryStream.of(loans)
                    .mapKeyValue(BigDecimal::multiply)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return weightedSum.divide(requestedAmount, roundingMode())
                    .setScale(Money.RATE_SCALE, roundingMode());
        }

        private BigDecimal toMonthlyInterestRate(BigDecimal annualInterestRate) {
            return annualInterestRate.divide(decimal(MONTHS_IN_YEAR), MONEY_CONTEXT);
        }
    }
}
