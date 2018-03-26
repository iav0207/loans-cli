package task.loans.calc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.util.streamex.EntryStream;
import javax.util.streamex.StreamEx;

import com.google.common.collect.ImmutableSortedMap;
import task.loans.model.LendingOffer;

@ParametersAreNonnullByDefault
public class LoanCalculator {

    private static final int MONTHS_IN_YEAR = 12;

    /**
     * Number of monthly payments.
     */
    private static final int REPAYMENTS = 36;

    /**
     * Default decimal arithmetic standard for monetary calculations.
     * Rounding mode: {@link java.math.RoundingMode#HALF_EVEN}.
     */
    private static final MathContext MONEY_CONTEXT = MathContext.DECIMAL128;

    private final SortedMap<BigDecimal, BigDecimal> offers;
    private final BigDecimal totalSupply;

    public LoanCalculator(List<LendingOffer> offers) {
        SortedMap<BigDecimal, BigDecimal> map = StreamEx.of(offers)
                .mapToEntry(LendingOffer::getRate, LendingOffer::getAmount)
                .toSortedMap(BigDecimal::add);

        this.offers = ImmutableSortedMap.copyOfSorted(map);
        this.totalSupply = offers.stream().map(LendingOffer::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Result calculate(BigDecimal lendingAmount) {
        if (totalSupply.compareTo(lendingAmount) < 0) {
            return Result.loanUnavailable();
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

        Result getResult() {
            BigDecimal monthlyRepayment = getMonthlyRepayment();
            return Result.builder()
                    .requestedAmount(requestedAmount)
                    .monthlyRepayment(monthlyRepayment)
                    .totalRepayment(monthlyRepayment.multiply(new BigDecimal(REPAYMENTS)))
                    .rate(calculateEffectiveRate())
                    .build();
        }

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
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private BigDecimal calculateMonthlyRepayment(BigDecimal principal, BigDecimal monthlyInterestRate) {
            BigDecimal r = BigDecimal.ONE.add(monthlyInterestRate).pow(REPAYMENTS);
            return monthlyInterestRate.multiply(principal).multiply(r)
                    .divide(r.subtract(BigDecimal.ONE), MONEY_CONTEXT);
        }

        private BigDecimal calculateEffectiveRate() {
            BigDecimal weightedSum = EntryStream.of(loans)
                    .mapKeyValue(BigDecimal::multiply)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return weightedSum.divide(requestedAmount, MONEY_CONTEXT);
        }

        private BigDecimal toMonthlyInterestRate(BigDecimal annualInterestRate) {
            return annualInterestRate.divide(new BigDecimal(MONTHS_IN_YEAR), MONEY_CONTEXT);
        }

        /**
         * @return {@code true} if the numbers are equal disregarding to {@link BigDecimal#scale()},
         * {@code false} otherwise.
         */
        private boolean numericallyEqual(BigDecimal a, BigDecimal b) {
            return a.compareTo(b) == 0;
        }
    }
}
