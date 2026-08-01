package rs.pgdavidov.erp.dashboard.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DashboardBankAccountResponse(

        UUID id,

        String code,

        String bankName,

        String accountNumber,

        String currencyCode,

        BigDecimal balance

) {
}