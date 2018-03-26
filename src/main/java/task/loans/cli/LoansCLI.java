package task.loans.cli;

import java.io.File;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.loans.core.Loan;
import task.loans.core.LoanCalculator;
import task.loans.io.CsvInputReader;
import task.loans.io.ResultFormatter;

import static task.loans.core.Money.decimal;

/**
 * Command line interface to loans repayment calculation.
 */
@ParametersAreNonnullByDefault
public class LoansCLI {

    private static final Logger logger = LoggerFactory.getLogger(LoansCLI.class);

    private final Params params;

    private LoansCLI(String[] args) {
        Parser parser = new Parser(LoansCLI.class.getCanonicalName());
        params = parser.parse(args);
    }

    public static void main(String... args) {
        try {
            new LoansCLI(args).run();
        } catch (RuntimeException ex) {
            logger.error(ex.getCause().getMessage());
            System.exit(1);
        }
    }

    private void run() {
        CsvInputReader reader = new CsvInputReader(params.skipLine, params.customSeparator);
        LoanCalculator calculator = new LoanCalculator(reader.read(new File(params.marketFile)));
        Loan result = calculator.calculate(decimal(params.loanAmount));
        logger.info("{}", new ResultFormatter().format(result));
    }

}
