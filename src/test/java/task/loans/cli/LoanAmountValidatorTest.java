package task.loans.cli;

import javax.annotation.ParametersAreNonnullByDefault;

import com.beust.jcommander.ParameterException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@ParametersAreNonnullByDefault
public class LoanAmountValidatorTest {

    private Params.LoanAmountValidator validator = new Params.LoanAmountValidator();

    @Test(dataProvider = "validAmountValues")
    public void positive(String amount) {
        validator.validate("amount", amount);
    }

    @Test(expectedExceptions = ParameterException.class, dataProvider = "invalidAmountValues")
    public void negative(String amount) {
        validator.validate("amount", amount);
    }

    @DataProvider(name = "validAmountValues")
    public static Object[] validAmountValues() {
        return new Object[] {
                "3500",
                "1000",
                "15000",
                "1100",
                "5900",
        };
    }

    @DataProvider(name = "invalidAmountValues")
    public static Object[] invalidAmountValues() {
        return new Object[] {
                "",
                "a",
                "0",
                "999",
                "1001",
                "7777",
                "900",
                "15100",
        };
    }

}
