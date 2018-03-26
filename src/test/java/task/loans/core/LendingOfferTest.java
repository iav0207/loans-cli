package task.loans.core;

import java.math.BigDecimal;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ParametersAreNonnullByDefault
public class LendingOfferTest {

    @Test(dataProvider = "negative", expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionOnConstruction(String lender, BigDecimal rate, BigDecimal amount) {
        new LendingOffer(lender, rate, amount);
    }

    @Test(dataProvider = "positive")
    public void shouldConstructFine(String lender, BigDecimal rate, BigDecimal amount) {
        LendingOffer offer = new LendingOffer(lender, rate, amount);

        assertThat(offer.getLenderName(), equalTo(lender));
        assertThat(offer.getRate(), equalTo(rate));
        assertThat(offer.getAmount(), equalTo(amount));
    }

    @DataProvider(name = "negative")
    public static Object[][] negative() {
        return new Object[][] {
                new TestCase().withLender(null).build(),
                new TestCase().withLender("").build(),
                new TestCase().withLender("  ").build(),

                new TestCase().withRate(null).build(),
                new TestCase().withRate(new BigDecimal("-0.1")).build(),
                new TestCase().withRate(new BigDecimal("-1")).build(),

                new TestCase().withAmount(null).build(),
                new TestCase().withAmount(new BigDecimal("-0.1")).build(),
                new TestCase().withAmount(new BigDecimal("-1")).build(),
        };
    }

    @DataProvider(name = "positive")
    public static Object[][] positive() {
        return new Object[][] {
                new TestCase().build(),

                // making no assumptions on first and last name, just checking it's not empty
                // names may be very different in number of words, characters used and names count
                new TestCase().withLender("a  ").build(),
                new TestCase().withLender("  -").build(),
                new TestCase().withLender("89").build(),

                new TestCase().withRate(BigDecimal.ZERO).build(),
                new TestCase().withRate(BigDecimal.ONE).build(),
                new TestCase().withRate(new BigDecimal("0.00001")).build(),

                new TestCase().withAmount(BigDecimal.ZERO).build(),
                new TestCase().withAmount(BigDecimal.ONE).build(),
                new TestCase().withAmount(new BigDecimal("0.00001")).build(),
        };
    }

    private static class TestCase {
        private static final String VALID_LENDER = "Abraham";
        private static final BigDecimal VALID_RATE = new BigDecimal("0.07");
        private static final BigDecimal VALID_AMOUNT = new BigDecimal("800");

        private String lender = VALID_LENDER;
        private BigDecimal rate = VALID_RATE;
        private BigDecimal amount = VALID_AMOUNT;

        TestCase withLender(@Nullable String name) {
            this.lender = name;
            return this;
        }

        TestCase withRate(@Nullable BigDecimal rate) {
            this.rate = rate;
            return this;
        }

        TestCase withAmount(@Nullable BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        Object[] build() {
            return new Object[] {lender, rate, amount};
        }
    }

}
