package task.loans.io;

import java.io.IOException;
import java.math.BigDecimal;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.loans.core.Loan;

@ParametersAreNonnullByDefault
public class ResultFormatter {

    private static final Logger logger = LoggerFactory.getLogger(ResultFormatter.class);

    private static final String RESOURCE_NAME = "result_format.txt";
    private static final String FORMAT = readFormatFromResource();
    private static final String UNAVAILABLE = "Lending for the specified amount is currently unavailable";

    public String format(Loan result) {
        if (result.getRate().compareTo(BigDecimal.ZERO) < 0) {
            return UNAVAILABLE;
        }
        return String.format(FORMAT, result.getRequestedAmount(), result.getRate().movePointRight(2),
                result.getMonthlyRepayment(), result.getTotalRepayment());
    }

    private static String readFormatFromResource() {
        try {
            return String.join("",
                    IOUtils.readLines(ResultFormatter.class.getResourceAsStream(RESOURCE_NAME), "utf-8"));
        } catch (IOException ioe) {
            logger.error("Could not read format string from resource");
            throw new RuntimeException(ioe);
        }
    }
}
