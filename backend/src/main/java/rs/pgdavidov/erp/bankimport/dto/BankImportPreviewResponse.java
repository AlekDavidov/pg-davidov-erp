package rs.pgdavidov.erp.bankimport.dto;

import rs.pgdavidov.erp.bankimport.model.ParsedBankTransaction;

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

        Integer transactionCount,

        List<ParsedBankTransaction> transactions

) {
    public BankImportPreviewResponse {
        transactions = transactions == null
                ? List.of()
                : List.copyOf(transactions);
    }
}