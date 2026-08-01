package rs.pgdavidov.erp.dashboard.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CashFlowPointResponse(

        YearMonth period,

        BigDecimal income,

        BigDecimal expense

) {
}