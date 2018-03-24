package task.loans.calc;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import task.loans.model.LendingOffer;

@ParametersAreNonnullByDefault
public class LoanCalculator {

    private final List<LendingOffer> offers;

    public LoanCalculator(List<LendingOffer> offers) {
        this.offers = offers;
    }

    public Result calculate(BigDecimal lendingAmount) {
        return Result.lendingFailed();
    }
}
