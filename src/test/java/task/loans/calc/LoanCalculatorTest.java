package task.loans.calc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import task.loans.model.LendingOffer;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

@ParametersAreNonnullByDefault
public class LoanCalculatorTest {

    @Test
    public void requestedMoreThanSupplied_negativeResult() {
        List<LendingOffer> offers = singletonList(new LendingOffer("Dave", dec("0.05"), dec("100")));
        Result result = new LoanCalculator(offers).calculate(dec("101"));

        assertThat(result.requestedAmount, lessThan(BigDecimal.ZERO));
    }

    @Test(dataProvider = "calculatorTestCases")
    public void checkResult(List<LendingOffer> offers, Result expected) {
        Result actual = new LoanCalculator(offers).calculate(expected.requestedAmount);
        softlyAssertEquals(actual, expected);
    }

    private static void softlyAssertEquals(Result actual, Result expected) {
        SoftAssert softly = new SoftAssert();
        softly.assertEquals(str(actual.requestedAmount),str(expected.requestedAmount));
        softly.assertEquals(str(actual.rate),str(expected.rate));
        softly.assertEquals(str(actual.totalRepayment),str(expected.totalRepayment));
        softly.assertEquals(str(actual.monthlyRepayment),str(expected.monthlyRepayment));
        softly.assertAll();
    }

    private static String str(BigDecimal decimal) {
        return format("%.2f", decimal);
    }

    private static boolean equal(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) == 0;
    }

    @DataProvider(name = "calculatorTestCases")
    public static Object[][] calculatorTestCases() {
        return new Object[][]{
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
        };
    }

    private static BigDecimal dec(String val) {
        return new BigDecimal(val);
    }

    private static class TestCase {
        private List<LendingOffer> offers = new ArrayList<>();
        private Result.Builder expectedResult = Result.builder();

        TestCase(String requestedAmount) {
            expectedResult.requestedAmount(dec(requestedAmount));
        }

        TestCase offer(String rate, String amount) {
            offers.add(new LendingOffer("Elizabeth", dec(rate), dec(amount)));
            return this;
        }

        TestCase rate(String expected) {
            expectedResult.rate(dec(expected));
            return this;
        }

        TestCase monthlyRepayment(String expected) {
            expectedResult.monthlyRepayment(dec(expected));
            return this;
        }

        TestCase totalRepayment(String expected) {
            expectedResult.totalRepayment(dec(expected));
            return this;
        }

        Object[] build() {
            return new Object[]{offers, expectedResult.build()};
        }
    }
}
