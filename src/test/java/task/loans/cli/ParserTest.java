package task.loans.cli;

import java.security.Permission;

import javax.annotation.ParametersAreNonnullByDefault;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ParametersAreNonnullByDefault
public class ParserTest {

    private final Parser parser = new Parser("");

    @BeforeMethod
    public void suppressExit() {
        // exit interception: throwing exception to fail test if unexpected exit occurs
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if (perm instanceof RuntimePermission && perm.getName().startsWith("exitVM."))
                    throw new ExitException();
            }
        });
    }

    private static class ExitException extends RuntimeException {
        ExitException() {
            super("Attempt to exit VM");
        }
    }

    @AfterMethod
    public void restore() {
        System.setSecurityManager(null);    // safe
    }

    @Test
    public void parse_positive() {
        Params params = parser.parse("file.csv", "-a", "3500");
        assertThat(params.marketFile, equalTo("file.csv"));
        assertThat(params.loanAmount, equalTo(3_500));
        assertThat(params.help, is(false));
        assertThat(params.skipLine, is(false));
        assertThat(params.customSeparator, nullValue());
    }

    @Test
    public void parse_lineSkipShort() {
        Params params = parser.parse("a", "-a", "100", "-l");
        assertThat(params.skipLine, is(true));
    }

    @Test
    public void parse_lineSkipLong() {
        Params params = parser.parse("a", "-a", "100", "--line-skip");
        assertThat(params.skipLine, is(true));
    }

    @Test
    public void parse_separatorShort() {
        Params params = parser.parse("bga", "-s", "=", "-a", "200");
        assertThat(params.customSeparator, equalTo('='));
    }

    @Test
    public void parse_separatorLong() {
        Params params = parser.parse("bga", "--sep", "=", "-a", "200");
        assertThat(params.customSeparator, equalTo('='));
    }

    @Test(expectedExceptions = ExitException.class)
    public void parse_helpEnabled_exits() {
        parser.parse("bga", "--sep", "=", "-h", "-a", "200");
    }

    @Test(expectedExceptions = ExitException.class)
    public void parse_invalidArgs_exits() {
        parser.parse("555");
    }

}
