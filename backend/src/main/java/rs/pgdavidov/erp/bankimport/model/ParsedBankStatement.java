package rs.pgdavidov.erp.bankimport.model;

import java.time.LocalDate;
import java.util.List;

public record ParsedBankStatement(

        String bankCode,

        String bankName,

        String statementId,

        String statementNumber,

        String accountNumber,

        LocalDate periodFrom,

        LocalDate periodTo,

        String currencyCode,

        List<ParsedBankTransaction> transactions

) {

    public ParsedBankStatement {
        transactions = transactions == null
                ? List.of()
                : List.copyOf(transactions);
    }
}