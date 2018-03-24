package task.loans.calc;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ParametersAreNonnullByDefault
public class ResultTest {

    private static final String referentialResult = readResultFromResource().trim();

    @Test(dataProvider = "results")
    public void checkFormat(Result result) {
        assertThat(result.toString(), equalTo(referentialResult));
    }

    @DataProvider(name = "results")
    public static Iterator<Object> results() {
        return stream(new String[][]{

                {"1000", "7.0", "30.78", "1108.10"},
                {"1000.0", "7", "030.7800", "1108.1"},

        }).map(arr -> Result.builder()
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
                    IOUtils.readLines(ResultTest.class.getResourceAsStream("referential_result.txt"),
                            "utf-8"));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
