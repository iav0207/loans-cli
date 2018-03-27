package task.loans.core;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static task.loans.core.Money.MONEY_CONTEXT;
import static task.loans.core.Money.decimal;
import static task.loans.core.Money.numericallyEqual;
import static task.loans.core.Money.rate;

@ParametersAreNonnullByDefault
public class LoanCalculatorTest {

    private static final Logger logger = LoggerFactory.getLogger(LoanCalculatorTest.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.#####");

    /**
     * Worst case relative accuracy of calculations.
     */
    private static final BigDecimal EPSILON = BigDecimal.valueOf(5e-4);

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
                new TestCase("1500")
                        .offer("0.2", "2000")
                        .offer("0.1", "750")
                        .rate("0.15")
                        .monthlyRepayment("52.00")
                        .totalRepayment("1871.89")
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
                        .offer("0.069", "111")
                        .offer("0.069", "289")
                        .offer("0.069", "180")
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
        BigDecimal absAvg = a.abs().add(b.abs()).divide(decimal(2), MONEY_CONTEXT);
        if (numericallyEqual(absAvg, BigDecimal.ZERO)) {
            return true;
        }
        logger.debug("{} == {}", df(a), df(b));
        BigDecimal delta = a.subtract(b).abs().divide(absAvg, MONEY_CONTEXT);
        logger.debug("delta = {}", df(delta));
        return delta.compareTo(EPSILON) < 0;
    }

    /**
     * Format decimal value: {@link #DECIMAL_FORMAT}
     */
    private static String df(BigDecimal decimal) {
        return DECIMAL_FORMAT.format(decimal);
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
