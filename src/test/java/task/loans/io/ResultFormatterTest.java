package task.loans.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import task.loans.core.Loan;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ParametersAreNonnullByDefault
public class ResultFormatterTest {

    private static final String referentialResult = readResultFromResource().trim();

    private ResultFormatter formatter = new ResultFormatter();

    @Test(dataProvider = "results")
    public void checkFormat(Loan loan) {
        assertThat(formatter.format(loan), equalTo(referentialResult));
    }

    @DataProvider(name = "results")
    public static Iterator<Object> results() {
        return stream(new String[][]{

                {"1000", "0.07", "30.78", "1108.10"},
                {"1000.0", "0.070", "030.7800", "1108.1"},

        }).map(arr -> Loan.builder()
                .requestedAmount(dec(arr[0]))
                .rate(dec(arr[1]))
                .monthlyRepayment(dec(arr[2]))
                .totalRepayment(dec(arr[3]))
                .build()
        ).map(Object.class::cast).iterator();
    }

    private static BigDecimal dec(String s) {
        return new BigDecimal(s);
    }

    private static String readResultFromResource() {
        try {
            return String.join("\n",
                    IOUtils.readLines(ResultFormatterTest.class.getResourceAsStream("referential_result.txt"),
                            "utf-8"));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
