package rs.pgdavidov.erp.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SupplierLedgerResponse(

        UUID supplierId,

        String supplierCode,

        String supplierName,

        String pib,

        LocalDate periodFrom,

        LocalDate periodTo,

        BigDecimal openingBalance,

        BigDecimal totalInvoiced,

        BigDecimal totalPaid,

        BigDecimal closingBalance,

        String currencyCode,

        List<SupplierLedgerEntryResponse> entries

) {
}