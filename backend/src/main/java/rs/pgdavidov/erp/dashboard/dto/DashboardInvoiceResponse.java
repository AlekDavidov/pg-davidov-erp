package rs.pgdavidov.erp.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DashboardInvoiceResponse(

        UUID id,

        String invoiceCode,

        String invoiceNumber,

        String supplierName,

        LocalDate dueDate,

        BigDecimal amount,

        BigDecimal paidAmount,

        BigDecimal remainingAmount,

        String currencyCode,

        String status

) {
}