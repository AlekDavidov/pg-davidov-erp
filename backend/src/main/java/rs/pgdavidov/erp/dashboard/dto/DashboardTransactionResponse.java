package rs.pgdavidov.erp.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DashboardTransactionResponse(

        UUID id,

        String transactionCode,

        LocalDate transactionDate,

        String description,

        String counterparty,

        BigDecimal debit,

        BigDecimal credit,

        String currencyCode

) {
}