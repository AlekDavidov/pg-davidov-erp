package rs.pgdavidov.erp.report.dto;

import java.time.LocalDate;
import java.util.List;

public record SupplierOutstandingBalancesResponse(

        LocalDate periodFrom,

        LocalDate periodTo,

        List<SupplierOutstandingBalanceResponse> suppliers

) {
}