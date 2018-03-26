package task.loans.cli;

import javax.annotation.ParametersAreNonnullByDefault;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ParametersAreNonnullByDefault
class Parser {

    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final String programName;

    Parser(String programName) {
        this.programName = programName;
    }

    Params parse(String... args) {
        Params params = new Params();
        JCommander commander = JCommander.newBuilder()
                .programName(programName)
                .addObject(params)
                .build();
        try {
            commander.parse(args);
        } catch (ParameterException exc) {
            printErrorAndExit(exc);
        }
        if (params.help) {
            commander.usage();
            System.exit(0);
        }
        return params;
    }

    private static void printErrorAndExit(ParameterException t) {
        logger.error("Wrong usage: " + t.getMessage());
        logger.error("See --help for details.");
        System.exit(1);
    }
}
