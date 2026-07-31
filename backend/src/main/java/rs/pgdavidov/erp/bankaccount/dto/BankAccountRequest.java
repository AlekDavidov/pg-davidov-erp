package rs.pgdavidov.erp.bankaccount.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BankAccountRequest(

        @NotBlank
        @Size(max = 150)
        String bankName,

        @NotBlank
        @Size(max = 100)
        String accountNumber,

        @NotBlank
        @Size(min = 3, max = 3)
        String currencyCode,

        @NotNull
        Boolean active
) {
}