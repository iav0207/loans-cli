package task.loans.cli;

import java.io.File;

import javax.annotation.ParametersAreNonnullByDefault;

import com.beust.jcommander.Parameter;
import org.apache.commons.lang3.builder.ToStringBuilder;

@ParametersAreNonnullByDefault
class Params {

    @Parameter(names = {"-h", "--help"}, help = true, description = "Display this page.")
    boolean help;

    @Parameter(order = 1, required = true, description = "Input file (CSV)")
    File marketFile;

    @Parameter(order = 2, required = true, names = {"-a", "--amount"}, description = "Loan amount (decimal)")
    Integer loanAmount;

    @Parameter(order = 3, names = {"-s", "--sep"}, description = "Custom CSV cells separator")
    Character customSeparator;

    @Parameter(order = 4, names = {"-l", "--line-skip"}, description = "Skip first line (header row) in CSV")
    boolean skipLine;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("help", help)
                .append("marketFile", marketFile)
                .append("loanAmount", loanAmount)
                .append("customSeparator", customSeparator)
                .append("skipLine", skipLine)
                .toString();
    }
}
