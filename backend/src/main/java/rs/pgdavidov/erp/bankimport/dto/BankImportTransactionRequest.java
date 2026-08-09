package rs.pgdavidov.erp.bankimport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BankImportTransactionRequest(

        @NotNull
        Integer entryNumber,

        @NotNull
        LocalDate transactionDate,

        @NotBlank
        String currencyCode,

        BigDecimal debit,

        BigDecimal credit,

        String counterparty,

        String description,

        String reference,

        UUID supplierId,

        UUID categoryId

) {
}