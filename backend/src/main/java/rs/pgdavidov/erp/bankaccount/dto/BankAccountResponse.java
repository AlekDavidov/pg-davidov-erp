package rs.pgdavidov.erp.bankaccount.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BankAccountResponse(

        UUID id,

        String code,

        String bankName,

        String accountNumber,

        String currencyCode,

        Boolean active,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}