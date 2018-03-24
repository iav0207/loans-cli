package task.loans.cli;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LoansCLI {

    private LoansCLI(String[] args) {
    }

    public static void main(String[] args) {
        new LoansCLI(args).run();
    }

    private void run() {
    }

}
