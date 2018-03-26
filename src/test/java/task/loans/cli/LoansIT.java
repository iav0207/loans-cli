package task.loans.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import task.loans.calc.Result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;

@ParametersAreNonnullByDefault
public class LoansIT {

    private PrintStream originalOut;
    private PrintStream originalErr;

    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeClass
    public void interceptStdOut() {
        originalOut = System.out;
        originalErr = System.err;
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @Test
    public void endToEndSmokeTest_positive() {
        Pattern expectedOutputPattern = Pattern.compile(
                "^\\s*Requested amount:.+Rate:.+Monthly repayment:.+Total repayment:.+$",
                Pattern.DOTALL);

        LoansCLI.main("example/market.csv", "-a", "1000", "-l");
        String output = out.toString();

        assertThat(err.toString(), isEmptyString());

        assertThat("Output was printed to stdout as expected.\nActual:\n" + output,
                expectedOutputPattern.matcher(output).matches());
    }

    @Test
    public void endToEndSmokeTest_negative() {
        LoansCLI.main("example/market.csv", "-a", "15000", "-l");
        String output = out.toString();

        assertThat(err.toString(), isEmptyString());

        assertThat(output, containsString(Result.loanUnavailable().toString()));
    }

    @AfterMethod
    public void resetStreams() {
        out.reset();
        err.reset();
    }

    @AfterClass
    public void resetStdOut() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

}
