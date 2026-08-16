package rs.pgdavidov.erp.report.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SupplierOutstandingBalanceResponse(

        UUID supplierId,

        String supplierCode,

        String supplierName,

        String pib,

        BigDecimal openingBalance,

        BigDecimal totalInvoiced,

        BigDecimal totalPaid,

        BigDecimal closingBalance,

        String currencyCode

) {
}