package task.loans.cli;

import javax.annotation.ParametersAreNonnullByDefault;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * CLI arguments.
 */
@ParametersAreNonnullByDefault
class Params {

    /**
     * Flag: help requested.
     */
    @Parameter(names = {"-h", "--help"}, help = true, description = "Display this page.")
    boolean help;

    /**
     * Input CSV market file.
     */
    @Parameter(required = true, description = "Input file (CSV)")
    String marketFile;

    /**
     * Requested loan amount.
     */
    @Parameter(required = true, names = {"-a", "--amount"}, description = "Loan amount (decimal)")
    Integer loanAmount;

    /**
     * Custom separator to use reading the CSV file.
     */
    @Parameter(names = {"-s", "--sep"}, description = "Custom CSV cells separator",
            converter = CharacterConverter.class)
    Character customSeparator;

    /**
     * Flag: start scanning CSV file from the second line.
     * Useful when the first line is header row.
     */
    @Parameter(names = {"-l", "--line-skip"}, description = "Skip first line (header row) in CSV")
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

    private static class CharacterConverter implements IStringConverter<Character> {
        @Override
        public Character convert(String string) {
            if (string.length() != 1) {
                throw new ParameterException("One character expected");
            }
            return string.charAt(0);
        }
    }
}
