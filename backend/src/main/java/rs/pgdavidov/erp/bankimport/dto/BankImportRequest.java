package rs.pgdavidov.erp.bankimport.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BankImportRequest(

        @NotBlank
        String bankCode,

        @NotBlank
        String accountNumber,

        @NotBlank
        String statementId,

        @NotBlank
        String originalFilename,

        @NotNull
        UUID bankAccountId,

        LocalDate periodFrom,

        LocalDate periodTo,

        @NotNull
        BigDecimal openingBalance,

        @NotNull
        BigDecimal totalIncome,

        @NotNull
        BigDecimal totalExpenses,

        @NotNull
        BigDecimal closingBalance,

        @NotEmpty
        List<
                @Valid
                @NotNull
                        BankImportTransactionRequest
                > transactions

) {
}