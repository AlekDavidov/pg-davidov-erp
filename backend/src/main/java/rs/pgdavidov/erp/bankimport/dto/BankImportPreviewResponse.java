package rs.pgdavidov.erp.bankimport.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BankImportPreviewResponse(

        String parserName,

        String bankCode,

        String bankName,

        String statementId,

        String statementNumber,

        String accountNumber,

        LocalDate periodFrom,

        LocalDate periodTo,

        String currencyCode,

        BigDecimal openingBalance,

        BigDecimal totalIncome,

        BigDecimal totalExpenses,

        BigDecimal closingBalance,

        Integer transactionCount,

        List<BankImportTransactionPreviewResponse> transactions

) {

    public BankImportPreviewResponse {
        transactions =
                transactions == null
                        ? List.of()
                        : List.copyOf(transactions);
    }
}