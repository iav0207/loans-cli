package task.loans.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static task.loans.core.Money.decimal;
import static task.loans.core.Money.numericallyEqual;
import static task.loans.core.Money.rate;
import static task.loans.core.Money.roundingMode;

@ParametersAreNonnullByDefault
public class LoanCalculatorTest {

    /**
     * Worst case relative accuracy of calculations.
     */
    private static final BigDecimal EPSILON = BigDecimal.valueOf(1e-3);

    @Test
    public void requestedMoreThanSupplied_negativeResult() {
        List<LendingOffer> offers = singletonList(new LendingOffer("Dave", rate("0.05"), decimal("100")));
        Loan loan = new LoanCalculator(offers).calculate(decimal("101"));

        assertThat(loan.getRate(), lessThan(BigDecimal.ZERO));
    }

    @Test(dataProvider = "calculatorTestCases")
    public void checkResult(List<LendingOffer> offers, Loan expected) {
        Loan actual = new LoanCalculator(offers).calculate(expected.getRequestedAmount());
        softlyAssertEquals(actual, expected);
    }

    @DataProvider(name = "calculatorTestCases")
    public static Object[][] calculatorTestCases() {
        return new Object[][]{
                new TestCase("1000")
                        .offer("0.1", "1500")
                        .offer("0.2", "2000")
                        .rate("0.1")
                        .monthlyRepayment("32.27")
                        .totalRepayment("1161.62")
                        .build(),
                new TestCase("3000")
                        .offer("0.1", "2000")
                        .offer("0.2", "2000")
                        .rate("0.133")
                        .monthlyRepayment("101.52")
                        .totalRepayment("3654.54")
                        .build(),
                new TestCase("5000")
                        .offer("0.0", "6000")
                        .rate("0.0")
                        .monthlyRepayment("138.89")
                        .totalRepayment("5000")
                        .build(),
                new TestCase("1000")    // sample data
                        .offer("0.075", "640")
                        .offer("0.069", "480")
                        .offer("0.071", "520")
                        .offer("0.104", "170")
                        .offer("0.081", "320")
                        .offer("0.074", "140")
                        .offer("0.071", "60")
                        .rate("0.07")
                        .monthlyRepayment("30.88")
                        .totalRepayment("1111.64")
                        .build(),
                new TestCase("1000")
                        .offer("0.071", "520")
                        .offer("0.069", "100")
                        .offer("0.069", "200")
                        .offer("0.069", "380")
                        .rate("0.07")
                        .monthlyRepayment("30.88")
                        .totalRepayment("1111.64")
                        .build(),
        };
    }

    private static void softlyAssertEquals(Loan actual, Loan expected) {
        SoftAssert softly = new SoftAssert();
        assertApproximatelyEqual(actual.getRequestedAmount(), expected.getRequestedAmount()).accept(softly);
        assertApproximatelyEqual(actual.getRate(), expected.getRate()).accept(softly);
        assertApproximatelyEqual(actual.getTotalRepayment(), expected.getTotalRepayment()).accept(softly);
        assertApproximatelyEqual(actual.getMonthlyRepayment(), expected.getMonthlyRepayment()).accept(softly);
        softly.assertAll();
    }

    private static Consumer<SoftAssert> assertApproximatelyEqual(BigDecimal actual, BigDecimal expected) {
        return softly -> softly.assertTrue(approximatelyEqual(actual, expected),
                format("Actual:   %.5f\n\tExpected: %.5f\n", actual, expected));
    }

    /**
     * Epsilon: {@link #EPSILON}
     */
    private static boolean approximatelyEqual(BigDecimal a, BigDecimal b) {
        BigDecimal absSum = a.abs().add(b.abs());
        if (numericallyEqual(absSum, BigDecimal.ZERO)) {
            return true;
        }
        BigDecimal delta = a.subtract(b).abs().divide(absSum.multiply(decimal(2)), roundingMode());
        return delta.compareTo(EPSILON) < 0;
    }

    private static class TestCase {
        private List<LendingOffer> offers = new ArrayList<>();
        private Loan.Builder expectedResult = Loan.builder();

        TestCase(String requestedAmount) {
            expectedResult.requestedAmount(decimal(requestedAmount));
        }

        TestCase offer(String rate, String amount) {
            offers.add(new LendingOffer("Elizabeth", Money.rate(rate), decimal(amount)));
            return this;
        }

        TestCase rate(String expected) {
            expectedResult.rate(Money.rate(expected));
            return this;
        }

        TestCase monthlyRepayment(String expected) {
            expectedResult.monthlyRepayment(decimal(expected));
            return this;
        }

        TestCase totalRepayment(String expected) {
            expectedResult.totalRepayment(decimal(expected));
            return this;
        }

        Object[] build() {
            return new Object[]{offers, expectedResult.build()};
        }
    }
}
