package rs.pgdavidov.erp.transaction.dto;

import rs.pgdavidov.erp.transaction.entity.TransactionSource;
import rs.pgdavidov.erp.transaction.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(

        UUID id,

        String transactionCode,

        LocalDate transactionDate,

        String currencyCode,

        BigDecimal debit,

        BigDecimal credit,

        String description,

        String rawCounterparty,

        UUID bankAccountId,

        UUID supplierId,

        UUID categoryId,

        UUID bankStatementRowId,

        String reference,

        TransactionStatus status,

        TransactionSource source,

        Boolean verified,

        String notes,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}