package task.loans.io;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import task.loans.core.LendingOffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@ParametersAreNonnullByDefault
public class CsvInputReaderTest {

    private final File valid = getResourceAsFile("valid.csv");
    private final File validNoHeader = getResourceAsFile("valid_no_header.csv");
    private final File validTabSep = getResourceAsFile("valid_tab_sep.csv");

    @Test
    public void valid_skipLine_parsesFine() {
        CsvInputReader reader = new CsvInputReader(true);
        List<LendingOffer> offers = reader.read(valid);
        assertThat(offers, notNullValue());
        assertThat(offers, hasSize(7));
    }

    @Test
    public void validNoHeader_resultEqualsToValid() {
        List<LendingOffer> expected = new CsvInputReader(true).read(valid);
        List<LendingOffer> actual = new CsvInputReader(false).read(validNoHeader);
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void validTabSep_resultEqualsToValid() {
        List<LendingOffer> expected = new CsvInputReader(true).read(valid);
        List<LendingOffer> actual = new CsvInputReader(true, '\t').read(validTabSep);
        assertThat(actual, equalTo(expected));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void fileNotFound_throwsException() {
        new CsvInputReader(false).read(new File("non.existent"));
    }

    @Test(dataProvider = "invalidCSVs", expectedExceptions = RuntimeException.class)
    public void invalidCsvFiles_throwsException(File csv) {
        new CsvInputReader(false).read(csv);
    }

    @DataProvider(name = "invalidCSVs")
    public static Iterator<Object> invalid() {
        return Stream.of(
                "invalid_1.csv",
                "invalid_2.csv",
                "invalid_3.csv",
                "invalid_4.csv")
                .map(res -> (Object) getResourceAsFile(res))
                .iterator();
    }

    private static File getResourceAsFile(String resourceName) {
        return new File(CsvInputReaderTest.class.getResource(resourceName).getFile());
    }

}
