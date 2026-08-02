package rs.pgdavidov.erp.bankimport.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedBankTransaction(

        Integer entryNumber,

        LocalDate transactionDate,

        LocalDate executionDate,

        BigDecimal debit,

        BigDecimal credit,

        BigDecimal balance,

        String currencyCode,

        String counterparty,

        String counterpartyAccount,

        String description,

        String reference,

        String orderType,

        String orderReference,

        Integer sourcePage

) {

    public boolean isIncome() {
        return credit != null
                && credit.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isExpense() {
        return debit != null
                && debit.compareTo(BigDecimal.ZERO) > 0;
    }
}