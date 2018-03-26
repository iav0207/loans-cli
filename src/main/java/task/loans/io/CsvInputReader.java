package task.loans.io;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import task.loans.core.LendingOffer;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static task.loans.core.Money.decimal;
import static task.loans.core.Money.rate;

/**
 * Reader extracting market data from CSV file.
 */
@ParametersAreNonnullByDefault
public class CsvInputReader {

    private static final Logger logger = LoggerFactory.getLogger(CsvInputReader.class);

    private final boolean skipFirstLine;
    private final char separator;

    /**
     * Create new reader instance with default CSV separator.
     *
     * @param skipFirstLine flag, if {@code true} – the first line of CSV file will not be read
     * @see CSVParser#DEFAULT_SEPARATOR
     */
    public CsvInputReader(boolean skipFirstLine) {
        this(skipFirstLine, null);
    }

    /**
     * Create new reader instance.
     *
     * @param skipFirstLine flag, if {@code true} – the first line of CSV file will not be read
     * @param separator     CSV file cells separator, if {@code null},
     *                      the default separator will be used – {@link CSVParser#DEFAULT_SEPARATOR}
     */
    public CsvInputReader(boolean skipFirstLine, @Nullable Character separator) {
        this.skipFirstLine = skipFirstLine;
        this.separator = Optional.ofNullable(separator).orElse(CSVParser.DEFAULT_SEPARATOR);
    }

    /**
     * Read market data from the input CSV file.
     *
     * @param csvFile input file to read data from
     * @return List of lending offers.
     * @see LendingOffer
     * @throws RuntimeException if any of these situations occurs:
     * <ul>
     *  <li>file not found</li>
     *  <li>IO exception caught</li>
     *  <li>data format is incorrect</li>
     * </ul>
     */
    public List<LendingOffer> read(File csvFile) {
        FileReader fileReader = onExceptionRethrow(() -> new FileReader(csvFile),
                "File not found: " + csvFile.getName());

        CSVReader csvReader = new CSVReader(fileReader, separator,
                CSVParser.DEFAULT_QUOTE_CHARACTER, skipFirstLine ? 1 : 0);

        return readAll(csvReader).stream().map(this::convert).collect(toList());
    }

    private List<String[]> readAll(CSVReader reader) {
        return onExceptionRethrow(reader::readAll, "Could not read input CSV file");
    }

    private LendingOffer convert(String[] row) {
        checkArgument(row.length == 3, "Invalid row length. Row: %s",
                stream(row).collect(joining(",", "[", "]")));

        return onExceptionRethrow(() -> new LendingOffer(row[0], rate(row[1]), decimal(row[2])),
                "Improper input data format");
    }

    private static <T> T onExceptionRethrow(MethodCall<T> methodCall, String failMessage) {
        try {
            return methodCall.get();
        } catch (Exception ex) {
            logger.error(failMessage);
            throw new InternalException(ex);
        }
    }

    private interface MethodCall<T> {
        @SuppressWarnings("squid:S00112")   // generic exception usage
        T get() throws Exception;
    }

    private static class InternalException extends RuntimeException {
        InternalException(Exception cause) {
            super(cause);
        }
    }

}
