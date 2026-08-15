package rs.pgdavidov.erp.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SupplierLedgerEntryResponse(

        LocalDate date,

        String type,

        UUID invoiceId,

        String invoiceNumber,

        UUID transactionId,

        String statementCode,

        String transactionReference,

        BigDecimal paidAmount,

        BigDecimal invoiceAmount,

        BigDecimal balance,

        String currencyCode

) {
}