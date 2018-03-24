package task.loans.cli;

import java.io.File;
import java.math.BigDecimal;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.loans.calc.LoanCalculator;
import task.loans.csv.CsvInputReader;

/**
 * Command line interface to loans repayment amount calculation.
 */
@ParametersAreNonnullByDefault
public class LoansCLI {

    private static final Logger logger = LoggerFactory.getLogger(LoansCLI.class);

    private final Params params;

    private LoansCLI(String[] args) {
        Parser parser = new Parser(LoansCLI.class.getCanonicalName());
        params = parser.parse(args);
    }

    public static void main(String[] args) {
        new LoansCLI(args).run();
    }

    private void run() {
        try {
            runSafely();
        } catch (RuntimeException ex) {
            logger.error(ex.getCause().getMessage());
            System.exit(1);
        }
    }

    private void runSafely() {
        CsvInputReader reader = new CsvInputReader(params.skipLine, params.customSeparator);
        LoanCalculator calculator = new LoanCalculator(reader.read(new File(params.marketFile)));
        logger.info(calculator.calculate(new BigDecimal(params.loanAmount)).toString());
    }

}
