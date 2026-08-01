package rs.pgdavidov.erp.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rs.pgdavidov.erp.transaction.entity.TransactionSource;
import rs.pgdavidov.erp.transaction.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(

        @NotNull
        LocalDate transactionDate,

        @NotBlank
        @Size(min = 3, max = 3)
        String currencyCode,

        @NotNull
        BigDecimal debit,

        @NotNull
        BigDecimal credit,

        String description,

        String rawCounterparty,

        UUID bankAccountId,

        UUID supplierId,

        UUID categoryId,

        UUID bankStatementRowId,

        String reference,

        @NotNull
        TransactionStatus status,

        @NotNull
        TransactionSource source,

        @NotNull
        Boolean verified,

        String notes
) {
}