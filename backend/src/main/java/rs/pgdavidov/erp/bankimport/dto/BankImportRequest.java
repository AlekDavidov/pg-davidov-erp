package rs.pgdavidov.erp.bankimport.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BankImportRequest(

        @NotBlank
        String bankCode,

        @NotBlank
        String accountNumber,

        @NotBlank
        String statementId,

        UUID bankAccountId,

        @NotEmpty
        List<
                @Valid
                @NotNull
                        BankImportTransactionRequest
                > transactions

) {
}